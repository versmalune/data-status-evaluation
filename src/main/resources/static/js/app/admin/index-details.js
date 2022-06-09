let $answersTemplate = $("#answersTemplate").html();
let $vrfcAnswersTemplate = $("#vrfcAnswersTemplate").html();
let $evidenceTemplate = $('#evidenceMaterialTemplate').html();

let $responseTypeTableBody = $("#responseTypeTableBody");
let $vrfcResponseTypeTableBody = $('#vrfcResponseTypeTableBody');
let $evidenceTableBody = $('#evidenceTableBody');

const addResponseTypes = (type) => {
    let currentTableBody;
    let tamplate;
    switch(type) {
        case 'instt':
            currentTableBody = $responseTypeTableBody;
            tamplate = $answersTemplate;
            break;
        case 'pmo':
            currentTableBody = $vrfcResponseTypeTableBody;
            tamplate = $vrfcAnswersTemplate
            break;
        default:
            console.error("기관/품질지원단 string이 잘못되었습니다.")
    }

    let children = currentTableBody.children();
    let currentData = {
        answers: []
    }

    for(let child of children) {
        let answerInput = $(child).find('td > .answer-input').get(0)
        let scoreInput = $(child).find('td >.score-input').get(0)
        let answer = {
            answer: $(answerInput).val(),
            score: $(scoreInput).val()
        }
        currentData.answers.push(answer);
    }

    currentData.answers.push({answer:'', score:''})

    let template= Handlebars.compile(tamplate);
    let html = template(currentData);
    currentTableBody.html(html);
}

const removeResponseType = (type, _index) => {
    let currentTableBody;
    let tamplate;
    switch(type) {
        case 'instt':
            currentTableBody = $responseTypeTableBody;
            tamplate = $answersTemplate;
            break;
        case 'pmo':
            currentTableBody = $vrfcResponseTypeTableBody;
            tamplate = $vrfcAnswersTemplate
            break;
        default:
            console.error("기관/품질지원단 string이 잘못되었습니다.")
    }
    let index = Number(_index)

    let children = currentTableBody.children();
    let currentData = {
        answers: []
    }
    let currentIndex = 0
    for(let child of children) {
        if(currentIndex === index) {
            currentIndex++;
            continue;
        }

        let answerInput = $(child).find('td > .answer-input').get(0)
        let scoreInput = $(child).find('td >.score-input').get(0)
        let answer = {
            answer: $(answerInput).val(),
            score: $(scoreInput).val()
        }
        currentData.answers.push(answer);
        currentIndex++;
    }

    let template= Handlebars.compile(tamplate);
    let html = template(currentData);
    currentTableBody.html(html);
}

const addEvidence = () => {
    let children = $evidenceTableBody.children();
    let currentData = {
        evidences: []
    }

    for(let child of children) {
        let nameInput = $(child).find('td > .evidence-name-input').get(0)
        let isRequiredInput = $(child).find('td >.evidence-checkbox-input').get(0)

        let name = $(nameInput).val();
        let isRequired = $(isRequiredInput).is(':checked');
        currentData.evidences.push({
            name,
            isRequired
        })
    }

    currentData.evidences.push({
        name: "",
        isRequired: false
    })
    let template= Handlebars.compile($evidenceTemplate);
    let html = template(currentData);
    $evidenceTableBody.html(html);
}

const removeEvidence = (_index) => {
    let index = Number(_index)
    let children = $evidenceTableBody.children();
    let currentData = {
        evidences: []
    }

    let i = 0;
    for(let child of children) {
        if(i === index) {
            i++;
            continue;
        }
        let nameInput = $(child).find('td > .evidence-name-input').get(0)
        let isRequiredInput = $(child).find('td >.evidence-checkbox-input').get(0)

        let name = $(nameInput).val();
        let isRequired = $(isRequiredInput).is(':checked');
        currentData.evidences.push({
            name,
            isRequired
        })
        i++;
    }

    let template= Handlebars.compile($evidenceTemplate);
    let html = template(currentData);
    $evidenceTableBody.html(html);
}

let $lowerTemplate = $('#lowerLevelTemplate');
let $upperTemplate = $('#upperLevelTemplate');
let $indexLevelRow = $('#indexLevelRow');
let $conditionDescInputRow = $('#conditionDescInputRow');

const onIndexLevelChange = (_this) => {
    let indexLevelTemplate;
    switch(_this.value) {
        case 'true':
            indexLevelTemplate = $lowerTemplate;
            $conditionDescInputRow.removeClass('d-none')
            break;
        case 'false':
            indexLevelTemplate = $upperTemplate;
            $conditionDescInputRow.addClass('d-none')
            break;
        default:
            console.error("지표 단계가 잘못되었습니다.");
            return;
    }

    let template= Handlebars.compile(indexLevelTemplate.html());
    let html = template('');
    $indexLevelRow.html(html);

    $('.index-number-input').change(function() {
        const parentNo = $('#indexNumber').val()
        const childNo = $('#childIndexNumber').val()
        $('#fullIndexNumber').val(`${parentNo}-${childNo}`);
    })
}

$(document).ready(() => {
    let $responseTypeTable = $("#responseTypeTable");
    let $responseTypeLabel = $("#responseTypeLabel");

    $("input[name='responseType']:radio").change(function () {
        switch(this.value) {
            case 'SINGLE_CHOICE':
                $responseTypeTable.removeClass('d-none');
                $responseTypeLabel.addClass('d-none');
                break;
            case 'MULTIPLE_CHOICE':
                $responseTypeTable.removeClass('d-none');
                $responseTypeLabel.addClass('d-none');
                break;
            case 'ESSAY':
                $responseTypeTable.addClass('d-none');
                $responseTypeLabel.removeClass('d-none');
                break;
            default:
                console.error("Response Type이 잘못되었습니다.")
        }
    })

    let $pmoResponseTypeTable = $("#pmoResponseTypeTable");
    let $pmoResponseTypeLabel = $("#pmoResponseTypeLabel");

    $("input[name='verificationResponseType']:radio").change(function () {
        switch(this.value) {
            case 'SINGLE_CHOICE':
                $pmoResponseTypeTable.removeClass('d-none');
                $pmoResponseTypeLabel.addClass('d-none');
                break;
            case 'MULTIPLE_CHOICE':
                $pmoResponseTypeTable.removeClass('d-none');
                $pmoResponseTypeLabel.addClass('d-none');
                break;
            case 'ESSAY':
                $pmoResponseTypeTable.addClass('d-none');
                $pmoResponseTypeLabel.removeClass('d-none');
                break;
            default:
                console.error("Response Type이 잘못되었습니다.")
        }
    })

    const parentNo = $('#indexNumber').val()
    const childNo = $('#childIndexNumber').val()
    $('#fullIndexNumber').val(`${parentNo}-${childNo}`);
})

