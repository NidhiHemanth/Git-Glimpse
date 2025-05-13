'use client';

import { useRef, useState, useCallback } from "react";
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
import { addNewUser } from "../api/github/addNewUser";

registerAgGridModules();

const DataTable = () => {
  const gridRef = useRef<AgGridReact<GithubUser>>(null);
  const [username, setUsername] = useState<String>('');
  const [toastMessage, setToastMessage] = useState<String>('');
  const [isLoading, setIsLoading] = useState(false);

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

  const showToast = (message: String) => {
    setToastMessage(message);
    setTimeout(() => setToastMessage(''), 3000); // hide after 3 seconds
  };

  const handleSubmitButton = async () => {
    if (!username.trim()) {
      showToast('Please enter a valid username.');
      return;
    }
    
    setIsLoading(true)
    const response = await addNewUser(username);
    console.log("GOT RESPONSE: ", response)
    showToast(response);
    setIsLoading(false)

    if(response === `User ${username} added successfully.`) {
      window.location.reload();
    }
  };

  const handleFormSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    handleSubmitButton();
  };

  return (
    <div style={{ height: '90vh', width: '100%', display: 'flex', flexDirection: 'column' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', margin: '0px 10px 10px 10px' }}>
        <form onSubmit={handleFormSubmit}>
          <input 
            type="text" 
            placeholder="Enter username" 
            onChange={(e) => setUsername(e.target.value)}
            style={{ padding: '5px', marginRight: '5px' }} 
          />
          <button className="submit-button" onClick={handleSubmitButton} disabled={isLoading}>
            {isLoading ? 'Adding user...' : 'Add User'}
          </button>
        </form>
        <button className="clear-button" onClick={handleClearFilters}>
          Clear Filters
        </button>
        {toastMessage && <div className="toast">{toastMessage}</div>}
      </div>
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