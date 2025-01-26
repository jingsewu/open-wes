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

export interface DashboardWidget {
  id: string;
  type: 'chart' | 'table' | 'kpi';
  title: string;
  query: string;
  visualization?: 'line' | 'bar' | 'pie';
  result?: QueryResult;
  insights?: string[];
  size?: {
    w: number;  // Width in grid columns (1-12)
    h: number;  // Height in grid rows
  };
}

export interface Dashboard {
  id: string;
  name: string;
  widgets: DashboardWidget[];
  userId?: string;  // To associate dashboard with user
}

export interface TabProps {
  isActive: boolean;
  onClose?: () => void;
}
