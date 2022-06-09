package kr.co.data_status_evaluation.util;

import org.modelmapper.ModelMapper;

import javax.print.attribute.standard.Destination;
import java.lang.reflect.Type;

public class ModelConverter {

    private static final ModelMapper modelMapper = new ModelMapper();

    public static <T> T convertObject(Object soruce, Type type) {
        return modelMapper.map(soruce, type);
    }
}
