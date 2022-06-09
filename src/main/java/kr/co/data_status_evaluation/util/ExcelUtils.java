package kr.co.data_status_evaluation.util;

import kr.co.data_status_evaluation.model.EvaluationField;
import kr.co.data_status_evaluation.model.EvaluationIndex;
import kr.co.data_status_evaluation.model.vo.InstitutionResultVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.*;
import org.hibernate.jdbc.Work;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
public class ExcelUtils {
    private int rowNum = 0;

    public List<Map<String, String>> getListData(MultipartFile file, int startRowNum, int columnLength) {

        List<Map<String, String>> excelList = new ArrayList<>();

        try {
            OPCPackage opcPackage = OPCPackage.open(file.getInputStream());

            @SuppressWarnings("resource")
            XSSFWorkbook workbook = new XSSFWorkbook(opcPackage);

            // 첫번째 시트
            XSSFSheet sheet = workbook.getSheetAt(0);

            int rowIndex = 0;
            int columnIndex = 0;

            for (rowIndex = startRowNum; rowIndex < sheet.getLastRowNum() + 1; rowIndex++) {
                XSSFRow row = sheet.getRow(rowIndex);

                if (row != null && row.getCell(0) != null && !row.getCell(0).toString().isEmpty()) {

                    Map<String, String> map = new HashMap<>();

                    int cells = columnLength;

                    for (columnIndex = 0; columnIndex <= cells; columnIndex++) {
                        XSSFCell cell = row.getCell(columnIndex);
                        DataFormatter df = new DataFormatter();
                        map.put(String.valueOf(columnIndex), df.formatCellValue(cell));
                    }

                    excelList.add(map);
                }
            }

        } catch (IOException e) {
            log.error("[" + e.getClass() + "] IOException Occured.");
        } catch (InvalidFormatException e) {
            log.error("[" + e.getClass() + "] InvalidFormatException Occured.");
        }

        return excelList;
    }

    public File createExcelToFile(String[] headers, List<Map<String, Object>> data, String filepath) throws IOException {
        Workbook workbook = new SXSSFWorkbook(); // excel 2007 이상
        Sheet sheet = workbook.createSheet("상담사례");

        rowNum = 0;

        createExcel(headers, sheet, data);

        File outputFile = File.createTempFile("temp", ".xlsx");
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            workbook.write(fos);
            workbook.close();
        }

        return outputFile;
    }

    private void createExcel(String[] headers, Sheet sheet, List<Map<String, Object>> data) {

        Row header = sheet.createRow(rowNum++);
        Cell headerCell;
        for (int i = 0; i < headers.length; i++) {
            headerCell = header.createCell(i);
            headerCell.setCellValue(headers[i]);
        }

        for (Map<String, Object> datum : data) {
            Row row = sheet.createRow(rowNum++);
            int cellNum = 0;

            for (String key : datum.keySet()) {
                Cell cell = row.createCell(cellNum++);
                cell.setCellValue(datum.get(key) == null ? "" : datum.get(key).toString());
            }
        }
    }

    public CellStyle setDefaultStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setWrapText(true);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        XSSFFont font = ((XSSFWorkbook) workbook).createFont();
        font.setFontName("Arial");
        font.setBold(true);
        style.setFont(font);

        return style;
    }

    public Workbook makeEvalResultTemplate(String year, List<EvaluationField> fields, List<EvaluationIndex> indices) {
        Workbook workBook = new XSSFWorkbook();
        Sheet sheet = workBook.createSheet("전체지표");
        Row headerRow1 = sheet.createRow(0);
        Row headerRow2 = sheet.createRow(1);
        CellStyle style = this.setDefaultStyle(workBook);

        String[] headerArr = {"기관코드", "기관명", "기관유형", "전년 결과", "전년 대비", year + "결과"};
        List<String> headers = Stream.concat(Arrays.stream(headerArr), indices.stream().map(EvaluationIndex::getNo)).collect(Collectors.toList());
        int lastCellNum = 0;
        for (int i = 0; i < headers.size(); i++) {
            Cell headerCell = headerRow1.createCell(lastCellNum);
            headerCell.setCellValue(headers.get(i));
            CellRangeAddress merge;
            if (headers.get(i).contains(year)) {
                merge = new CellRangeAddress(0, 0, lastCellNum, lastCellNum + fields.size());
                for (int j = 0; j < fields.size() + 1; j++) {
                    Cell fieldCell = headerRow2.createCell(j + lastCellNum);
                    if (j == 0) {
                        fieldCell.setCellValue("최종점수");
                    } else {
                        String fieldName = fields.get(j - 1).getName();
                        fieldCell.setCellValue(fieldName);
                    }
                    fieldCell.setCellStyle(style);
                    sheet.setColumnWidth(j + lastCellNum, 4000);
                }
                lastCellNum += fields.size() + 1;
            } else if (headers.get(i).contains("-")) {
                merge = new CellRangeAddress(0, 0, lastCellNum, lastCellNum + 2);
                for (int j = 0; j < 3; j++) {
                    Cell indexCell = headerRow2.createCell(lastCellNum + j);
                    if (j == 0) {
                        indexCell.setCellValue("등급");
                    } else if (j == 1) {
                        indexCell.setCellValue("배점");
                    } else {
                        indexCell.setCellValue("점수");
                    }
                    indexCell.setCellStyle(style);
                    sheet.setColumnWidth(lastCellNum + j, 4000);
                }
                lastCellNum += 3;
            } else {
                merge = new CellRangeAddress(0, 1, lastCellNum, lastCellNum);
                sheet.setColumnWidth(lastCellNum, 4000);
                lastCellNum++;
            }
            sheet.addMergedRegion(merge);
            headerCell.setCellStyle(style);
        }

        return workBook;
    }
}
