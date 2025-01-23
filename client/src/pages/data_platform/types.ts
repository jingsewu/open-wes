export interface QueryResult {
  sql: string;
  data: any[];
  columns: ColumnMetadata[];
}

export interface QueryError {
  message: string;
  sql?: string;
}

export interface ColumnMetadata {
  name: string;
  dataType: string;
  description: string;
}

export interface Message {
  role: 'user' | 'assistant';
  content: string;
}
