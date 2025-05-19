import { GithubUser } from "./GithubUser";

export interface SortColumn {
  column: string,
  sortDirection: 'ascending' | 'descending'
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
  startRow: number;
  endRow: number;
  columnFilters: (StringColumnFilter | NumericColumnFilter)[];
  sortColumns: SortColumn[];
}

export interface FetchResponse {
  githubUsers: GithubUser[],
  lastRow: number
}