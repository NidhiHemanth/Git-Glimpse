'use client';

import { useRef, useCallback } from "react";
import { GridReadyEvent } from "ag-grid-community";
import { AgGridReact } from "ag-grid-react";
import { columnDefs } from "./config/ColumnDefs";
import { CustomTooltip } from "./config/CustomToolTip";
import { defaultColumnDefs } from "./config/DefaultColumnDefs";
import { FetchRequest } from "../api/types/FetchRequest";
import { fetchUsers } from "../api/github/fetchUsers";
import NoRowsOverlay from "./NoRowsOverlay";
import { GithubUser } from "../api";
import { registerAgGridModules } from "./agGridModules";

registerAgGridModules();

const DataTable = () => {
  const gridRef = useRef<AgGridReact<GithubUser>>(null);
  
  const onGridReady = useCallback((params: GridReadyEvent) => {
    params.api.setGridOption('serverSideDatasource', datasource);
  }, [])

  const datasource = {
    getRows(params: any) {
      const { startRow, endRow, filterModel, sortModel } = params.request;
      const numericColumns = ['followers', 'following'];
      let request: FetchRequest = {
        lastId: -1,
        size: 20,
        columnFilters: [],
        sortColumns: []
      }

      if(sortModel.length) {
        const { colId, sort } = sortModel[0]
        request.sortColumns.push({
          column: colId,
          sortDirection: sort === 'asc' ? "ascending" : "descending"
        })
        console.log(`Sort Model: ${JSON.stringify(sortModel)}, Request: ${JSON.stringify(request.sortColumns)}`);
      }

      const filterKeys = Object.keys(filterModel)
      filterKeys.forEach((filter) => {
        request.columnFilters.push({
          column: filter,
          value: filterModel[filter]['filter'],
          columnFilterType: numericColumns.includes(filter) ? 'numericColumnFilter' : 'stringColumnFilter',
          operator: filterModel[filter]['type']
        })
        console.log(`Filter Model: ${JSON.stringify(filterModel)}, Request: ${JSON.stringify(request.columnFilters)}`);
      })

      fetchUsers(request)
      .then((response) => {
        console.log("RESPONSE = ", response);

        const rowData = response.githubUsers || [];
        if (rowData.length === 0) {
          params.api.showNoRowsOverlay();
        } else {
          params.api.hideOverlay();
        }
  
        params.success({
          rowData: response.githubUsers
        });

        setTimeout(() => {
          console.log("Auto-sized columns username & email successfully")
          params.api.autoSizeColumns(['username', 'fullName']);
        }, 50);
      })
      .catch((error) => {
        console.error(error);
        params.api.showNoRowsOverlay();
        params.fail()
      })
    },
  };

  const handleClearFilters = useCallback(() => {
    if (gridRef.current) {
      const blankFilterColumns = ['email', 'mostStarredProjectName']
      gridRef.current.api.setFilterModel(null);
      gridRef.current.api.applyColumnState({
        defaultState: { sort: null },
        applyOrder: true,
      });
      blankFilterColumns.forEach(column => {
        gridRef.current?.api.destroyFilter(column);
      })
      gridRef.current.api.onFilterChanged();
      gridRef.current.api.onSortChanged();
      gridRef.current.api.refreshServerSide({ purge: true });
      console.log("Unset all the column filters successfully")
    }
  }, []);

  // Container: Defines the grid's theme & dimensions.
  return (
    <div style={{ height: '90vh', width: '100%', display: 'flex', flexDirection: 'column' }}>
      <button style={{ padding: '5px 10px', marginBottom: '10px', cursor: 'pointer' }} onClick={handleClearFilters}>
        Clear Filters
      </button>
      <div style={{ flex: 1, overflow: 'hidden' }} className="ag-theme-alpine">
        <AgGridReact<GithubUser>
          ref={gridRef}
          rowModelType={'serverSide'}
          //serverSideInfiniteScroll={true}
          infiniteInitialRowCount={20}
          rowBuffer={0}
          cacheBlockSize={100}
          cacheOverflowSize={2}
          maxConcurrentDatasourceRequests={1}
          maxBlocksInCache={5}
          onGridReady={onGridReady}
          gridOptions={{ noRowsOverlayComponent: NoRowsOverlay }}
          //datasource={datasource}
          columnDefs={columnDefs}
          defaultColDef={defaultColumnDefs}
          components={{ customTooltip: CustomTooltip }}
          tooltipShowDelay={0}
          suppressMenuHide={false}
          overlayNoRowsTemplate={'<no-rows-overlay'}
          debug={true}
        />
      </div>
    </div>
  );
};

export default DataTable;