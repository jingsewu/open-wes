package org.openwes.ai.core.template;

public class AiPromptTemplate {

    public static final String QA_QUESTION_CLARIFY_TEMPLATE = """
            判断下面问题类型：1.普通咨询 2.需函数调用
            {question}
            输出格式: 输出数字1或者2
            例如: 1
            """;

    public static final String QA_TOOL_CALL_TEMPLATE = """
            用户问题：{question}
            历史上下文：{context}
            输出语言：{language}
            """;

    public static final String QA_PROMPT_TEMPLATE = """
            您好！欢迎使用 Open warehouse execution system 问答助手。为了精准帮您解决问题，请您尽量详细地描述您遇到的情况，例如：
            您在系统操作过程中的具体步骤，当问题出现时您正在进行什么操作（如货物入库登记、库存查询、订单分配等）。
            系统反馈给您的错误提示信息，一字不差地提供给我们。
            您期望达成的目标，是想要了解某个功能如何使用，还是排查系统报错原因，亦或是寻求优化工作流程的建议等。
            您的问题描述：{question}
            
            历史信息：{context}
            
            回答输出格式：
            [详细、清晰且步骤分明的回答内容，根据不同问题类型，若为操作问题，给出操作步骤截图（如有）、图文并茂的指引；若为报错排查，指出可能原因及对应的解决办法；若为流程优化，对比现有流程说明优化点及优势等]
            
            输出语言：{language}
            
            """;


    public static final String SQL_PROMPT_TEMPLATE = """
            Based on the following database schema:
            
            {schema}
            
            Previous context:
            {context}
            
            Generate a SQL query that: {description}
            
            Previous failed attempts and their errors:
            {previousErrors}
            
            Table Relationship Rules:
            1. Primary Key References:
               - Any column named 'id' is the primary key of its table
               - A column named 'xxx_id' always references the 'id' column of table 'w_xxx'
               Example: inbound_plan_order_id references w_inbound_plan_order.id
            
            2. Code References:
               - warehouse_code → m_warehouse_main_data.warehouse_code
               - owner_code → m_owner_main_data.owner_code
               - sku_code → m_sku_main_data.sku_code
               - sku_id → m_sku_main_data.id
            
            Column Data Type Rules:
            1. Time/Date Columns:
               - All time/date columns store Unix timestamp in milliseconds (BIGINT)
               - Use FROM_UNIXTIME(column_name/1000) to convert to datetime
               - Use DATE(FROM_UNIXTIME(column_name/1000)) to convert to date
               - Exception: Columns explicitly defined as DATE or DATETIME type
            
            Additional Requirements:
            1. Include all necessary columns to provide comprehensive results
            2. Rename columns using clear, descriptive aliases for easy understanding
            3. Ensure column names are unique and meaningful
            
            When joining tables:
            1. Always use the exact reference patterns above
            2. Never join using business columns (like order_no, batch_no) unless explicitly requested
            3. For master data tables (m_xxx), always join using the _code or _id columns as specified above
            
            Please ensure the query:
            1. Uses the correct table and column names as shown in the schema
            2. Includes appropriate JOINs where needed
            3. Follows SQL best practices
            4. Is optimized for performance
            5. Avoids the errors encountered in previous attempts
            
            Strictly follow this output format:
            ### SQL ###
            [Your generated SQL query here, using proper MySQL syntax]
            ### END SQL ###
            
            ### EXPLANATION ###
            [Concise technical explanation of the SQL query structure]
            [Description of key elements: SELECT, WHERE, JOINs, etc.]
            [Note about potential limitations or assumptions]
            ### END EXPLANATION ###"
            
            **Example Output:**
            ### SQL ###
            SELECT department, COUNT(*) as employee_count
            FROM employees
            WHERE hire_date >= '2023-01-01'
            GROUP BY department
            ORDER BY employee_count DESC;
            ### END SQL ###
            
            ### EXPLANATION ###
            1. Selects department names and counts employees per department
            2. Filters for employees hired since 2023-01-01
            3. Groups results by department
            4. Orders by employee count descending
            5. Assumes 'hire_date' field exists in employees table
            ### END EXPLANATION ###"
            
            """;


    public static final String JAVA_SCRIPT_COVERT_PROMPT_TEMPLATE = """
            
            Write a JavaScript function named convert to transform an upstream message into a target format. The function should:
            
            1. Accept an upstream message as a parameter in JSON format.
            2. Process the upstream message fields according to specific rules:
               a. Include the mapping rules between upstream fields and target fields (list them explicitly or as examples).
               b. Apply transformations where necessary (e.g., data type conversions, unit conversions, or renaming keys).
               c. Handle optional fields gracefully (e.g., omit or set defaults if missing).
            3. Return the transformed message as JSON in the target format.
            Example Input Message (Upstream):
            {inputFormat}
            
            Example Target Format:
            {outputFormat}
            
            Transformation Rules:
            {transformationRules}
            
            """;


    public static final String JAVA_CODE_COMPLETION_PROMPT_TEMPLATE = """
            
            You are an AI that helps developers write {language} code.
            The user is writing code, and you need to provide an intelligent auto-complete suggestion.
            Here is the existing code:
            ```
            {codeContext}
            ```
            The current line they are typing:
            ```
            {lineContent}
            ```
            Provide only the suggested completion (do not repeat the input).
            
            """;

}
