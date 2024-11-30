package org.openwes.common.utils.exception.code_enum;

import org.openwes.common.utils.constants.AppCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BasicErrorDescEnum implements IBaseError {

    BASIC_ERROR_DESC_ENUM("BA001001", "basic base error", AppCodeEnum.WMS.name()),

    // container error
    CONTAINER_NOT_EXIST("BA001002", "container {0} not exist", AppCodeEnum.WMS.name()),
    CONTAINER_SLOT_NOT_EXIST("BA001003", "container slot {0} not exist", AppCodeEnum.WMS.name()),
    CONTAINER_SPECIFIC_NOT_EXIST("BA001004", "container specific not exist", AppCodeEnum.WMS.name()),
    CONTAINER_SPECIFIC_CANNOT_CHANGE("BA001005", "container specific cannot change, maybe it's already in use", AppCodeEnum.WMS.name()),
    CONTAINER_SPECIFIC_SLOT_CODE_REPEAT("BA001005", "container specific slot code repeat", AppCodeEnum.WMS.name()),
    CONTAINER_SPECIFIC_SLOT_LEVEL_BAY_REPEAT("BA001006", "container specific slot loc level bay repeat", AppCodeEnum.WMS.name()),
    CONTAINER_IS_PUT_AWAY("BA001007", "container is putted away", AppCodeEnum.WMS.name()),
    CONTAINER_CONTAINS_STOCK("BA001008", "container contains stocks", AppCodeEnum.WMS.name()),
    CONTAINER_DID_NOT_MEET_THE_CONDITIONS("BA001009", "container did not meet the conditions", AppCodeEnum.WMS.name()),
    CONTAINER_SPEC_CODE_NOT_EXISTS("BA001010", "container specification code does not exist", AppCodeEnum.WMS.name()),
    THERE_IS_NO_CONTAINER_THAT_MEETS_THE_CONDITIONS("BA001011", "there is no container that meets the conditions", AppCodeEnum.WMS.name()),
    THERE_IS_NO_CONTAINER_THAT_MEETS_THE_CONDITIONS_NOT_ENOUGH_STOCK("BA001012", "there is no container that meets the conditions sku {0} stock not exists", AppCodeEnum.WMS.name()),
    CONTAINER_HAS_UNAVAILABLE_STOCK("BA001013", "the container {} has unavailable stocks", AppCodeEnum.WMS.name()),
    CONTAINER_SPECIFIC_CANNOT_DELETE("BA001014", "container specific cannot delete", AppCodeEnum.WMS.name()),

    // location error
    LOCATION_CONTAINS_STOCK("BAS010001", "location contains stocks", AppCodeEnum.WMS.name()),
    FORBIDDEN_OPERATE_MULTIPLE_AISLE("BAS010002", "forbidden operate multiple aisles", AppCodeEnum.WMS.name()),

    //warehouse
    WAREHOUSE_LOGIC_CONTAINER_LOCATION("BAS020001", "warehouse logic {0} contains locations", AppCodeEnum.WMS.name()),
    WAREHOUSE_LOGIC_CODE_REPEATED("BAS020002", "warehouse logic code {0} exists", AppCodeEnum.WMS.name()),
    WAREHOUSE_AREA_CODE_REPEATED("BAS020003", "warehouse area code {0} exists", AppCodeEnum.WMS.name()),
    WAREHOUSE_CODE_REPEATED("BAS020004", "warehouse area code {0} exists", AppCodeEnum.WMS.name()),
    WAREHOUSE_STORAGE_AREA_NOT_FOUND("BAS020005", "warehouse storage area not found, please set a storage area at least", AppCodeEnum.WMS.name()),

    // work location error
    WORK_LOCATION_NOT_EXIST("BAL030001", "work location {0} does not exist", AppCodeEnum.WMS.name()),

    // main data
    MAIN_DATA_BASE_ERROR("MD001001", "main data base error", AppCodeEnum.WMS.name()),
    CODE_MUST_NOT_UPDATE("MD001002", "code must not update", AppCodeEnum.WMS.name()),

    WAREHOUSE_CODE_NOT_EXIST("MD002001", "warehouse code {0} not exist", AppCodeEnum.WMS.name()),
    WAREHOUSE_CODE_EXIST("MD002002", "warehouse code {0} exist", AppCodeEnum.WMS.name()),
    WAREHOUSE_NOT_EXIST("MD002003", "warehouse not exist", AppCodeEnum.WMS.name()),

    OWNER_CODE_NOT_EXIST("MD003001", "owner code {0} not exist", AppCodeEnum.WMS.name()),
    OWNER_NOT_EXIST("MD003002", "owner not exist", AppCodeEnum.WMS.name()),

    SKU_CODE_NOT_EXIST("MD004001", "sku code {0} not exist", AppCodeEnum.WMS.name()),
    SOME_SKU_CODE_NOT_EXIST("MD004002", "some sku codes not exist", AppCodeEnum.WMS.name()),
    BARCODE_NOT_EXIST("MD004003", "barcode not exists", AppCodeEnum.WMS.name()),
    SKU_NOT_EXIST("MD004004", "sku not exists", AppCodeEnum.WMS.name()),
    DUPLICATE_SKU("MD004005", "duplicate sku", AppCodeEnum.WMS.name()),

    BARCODE_PARSER_RESULT_CONFIG_NOT_MATCH("MD005001", "barcode parse result field size & config field size not match", AppCodeEnum.WMS.name()),
    BARCODE_PARSE_RULE_REPEAT("MD005002", "barcode parse rule repeated", AppCodeEnum.WMS.name()),


    BATCH_ATTRIBUTE_NOT_EXISTS("MD006001", "batchAttribute {0} not exist", AppCodeEnum.WMS.name()),
    BATCH_ATTRIBUTE_IS_REQUIRED("MD006002", "batch attribute {0} can not emtpy", AppCodeEnum.WMS.name());

    private final String code;
    private final String desc;
    private final String appCode;
}
