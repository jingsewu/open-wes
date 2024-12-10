export const warehouse_code_search_api = {
    "method": "post",
    "url": "/search/search/searchSelectResult?perPage=1000&activePage=1",
    "data": {
        "searchIdentity": "SearchWarehouse",
        "searchObject": {
            "tables": "m_warehouse_main_data"
        },
        "showColumns": [
            {
                "dbField": "warehouse_code",
                "name": "value",
                "javaType": "java.lang.String"
            },
            {
                "dbField": "warehouse_name",
                "name": "label",
                "javaType": "java.lang.String"
            }
        ]
    }
}

export const role_search_api = {
    "method": "post",
    "url": "/search/search/searchSelectResult?perPage=1000&activePage=1",
    "data": {
        "searchIdentity": "SearchRole",
        "searchObject": {
            "tables": "u_role",
            "where": "status = 1"
        },
        "showColumns": [
            {
                "dbField": "id",
                "name": "value",
                "javaType": "java.lang.String"
            },
            {
                "dbField": "name",
                "name": "label",
                "javaType": "java.lang.String"
            }
        ]
    }
}

export const menu_search_api = {
    "method": "post",
    "url": "/search/search/searchSelectResult?perPage=1000&activePage=1",
    "data": {
        "searchIdentity": "SearchMenu",
        "searchObject": {
            "tables": "u_menu"
        },
        "showColumns": [
            {
                "dbField": "id",
                "name": "value",
                "javaType": "java.lang.String"
            },
            {
                "dbField": "title",
                "name": "label",
                "javaType": "java.lang.String"
            }
        ]
    }
}
