'use client';

import { useRef, useEffect, useCallback } from "react";
import { ModuleRegistry, GridReadyEvent } from "ag-grid-community";
import { ServerSideRowModelModule } from "ag-grid-enterprise";
import { AgGridReact } from "ag-grid-react";
import { columnDefs } from "./config/ColumnDefs";
import { CustomTooltip } from "./config/CustomToolTip";
import { defaultColumnDefs } from "./config/DefaultColumnDefs";
import { FetchRequest } from "../api/types/FetchRequest";
import { fetchUsers } from "../api/github/fetchUsers";

ModuleRegistry.registerModules([ServerSideRowModelModule]);

const DataTable = () => {
  const gridRef = useRef<AgGridReact>(null);
  const onGridReady = useCallback((params: GridReadyEvent) => {
    params.api.setGridOption('serverSideDatasource', datasource);
  }, [])

  const datasource = {
    getRows(params: any) {
      const { startRow, endRow, filterModel, sortModel } = params.request;
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
          direction: sort.toUpperCase()
        })
        console.log(`Sort Model: ${JSON.stringify(sortModel)}, Request: ${JSON.stringify(request.sortColumns)}`);
      }

      const filterKeys = Object.keys(filterModel)
      filterKeys.forEach((filter) => {
        request.columnFilters.push({
          column: filter,
          value: filterModel[filter]['filter'],
          columnFilterType: 'stringColumnFilter',
          operator: filterModel[filter]['type']
        })
        console.log(`Filter Model: ${JSON.stringify(filterModel)}, Request: ${JSON.stringify(request.columnFilters)}`);
      })

      fetchUsers(request)
      .then((response) => {
        console.log("RESPONSE = ", response);
        params.success({
          rowData: response.githubUsers
        });
      })
      .catch((error) => {
        console.error(error);
        params.fail()
      })
    },
  };

  useEffect(() => {
    // Auto size the columns after data is loaded
    // For this to work, we have to unset flex in columnDefs for these columns
    if (gridRef.current) {
      gridRef.current.api.autoSizeColumns(['username', 'fullName']); // Auto-size specific columns
    }
  }, []);

  // Container: Defines the grid's theme & dimensions.
  return (
    <div style={{ height: '90%', width: '100%', overflowX: 'auto' }} className="ag-theme-alpine">
      <AgGridReact
        rowModelType={'serverSide'}
        //serverSideInfiniteScroll={true}
        infiniteInitialRowCount={20}
        rowBuffer={0}
        cacheBlockSize={100}
        cacheOverflowSize={2}
        maxConcurrentDatasourceRequests={1}
        maxBlocksInCache={5}
        onGridReady={onGridReady}
        //datasource={datasource}
        columnDefs={columnDefs}
        defaultColDef={defaultColumnDefs}
        components={{ customTooltip: CustomTooltip }}
        tooltipShowDelay={0}
        debug={true}
      />
    </div>
  );
};

export default DataTable;