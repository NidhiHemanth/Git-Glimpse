export interface SortColumn {
  column: string,
  direction: 'ascending' | 'descending'
}

export interface ColumnFilter {
  column: string,
  columnFilterType: 'stringColumnFilter' | 'numericColumnFilter'
}

export interface StringColumnFilter extends ColumnFilter {
  value: string;
  operator: 'equal' | 'notEqual' | 'contains';
  columnFilterType: 'stringColumnFilter';
}

export interface NumericColumnFilter extends ColumnFilter {
  value: number;
  toValue?: number;
  operator: 'equal' | 'notEqual' | 'lesserThan' | 'greaterThan' | 'toValue';
  columnFilterType: 'numericColumnFilter';
}

export interface FetchRequest {
  lastId: number;
  size: number;
  columnFilters: (StringColumnFilter | NumericColumnFilter)[];
  sortColumns: SortColumn[];
}
