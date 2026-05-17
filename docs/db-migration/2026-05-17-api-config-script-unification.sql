-- Migration: unify api_config converter script columns
-- Merges js_param_converter + template_param_converter → param_converter_script
-- Merges js_response_converter + template_response_converter → response_converter_script
-- Run against the wes database before deploying the new server version.

-- Step 1: add unified columns
ALTER TABLE a_api_config
  ADD COLUMN param_converter_script    TEXT COMMENT '参数转换脚本',
  ADD COLUMN response_converter_script TEXT COMMENT '响应转换脚本';

-- Step 2: migrate existing data (JS takes priority; falls back to TEMPLATE)
UPDATE a_api_config
SET param_converter_script    = COALESCE(js_param_converter, template_param_converter),
    response_converter_script = COALESCE(js_response_converter, template_response_converter);

-- Step 3: drop old columns
ALTER TABLE a_api_config
  DROP COLUMN js_param_converter,
  DROP COLUMN js_response_converter,
  DROP COLUMN template_param_converter,
  DROP COLUMN template_response_converter;

-- Step 4: add JAVA to ConverterType dictionary (for existing installations)
UPDATE m_dictionary
SET items = JSON_ARRAY_INSERT(
    items,
    '$[2]',
    JSON_OBJECT(
        'order', 0,
        'value', 'JAVA',
        'defaultItem', false,
        'description', JSON_OBJECT('languages', JSON_OBJECT('en-US', 'Java', 'zh-CN', NULL)),
        'showContext', JSON_OBJECT('languages', JSON_OBJECT('en-US', 'Java', 'zh-CN', 'java'))
    )
)
WHERE code = 'ConverterType';
