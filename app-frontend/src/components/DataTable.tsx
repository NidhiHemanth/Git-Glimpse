'use client';

import { useState, useRef, useEffect } from "react";
import { AllCommunityModule, ModuleRegistry } from "ag-grid-community";
import { AgGridReact } from "ag-grid-react";
import { GithubUser } from "../api";
import { columnDefs } from "./config/ColumnDefs";
import sampleData from '../data/sample.json';
import { useFetchGithubUser } from '../api/github/findGithubUser'
import { CustomTooltip } from "./config/CustomToolTip";

ModuleRegistry.registerModules([AllCommunityModule]);

// Create new GridExample component
const DataTable = () => {
  const gridRef = useRef<any>(null);

  const { user, loading } = useFetchGithubUser("sn99");
  console.log(user, loading)

  const [rowData, setRowData] = useState<GithubUser[]>(sampleData);
  useEffect(() => {
    // Auto size the columns after data is loaded
    // For this to work, we have to unset flex in columnDefs for these columns
    if (gridRef.current) {
      gridRef.current.api.autoSizeColumns(['username', 'fullName']); // Auto-size specific columns
    }
  }, [rowData]);

  useEffect(() => {
    if (user) {
      // Wrap the single user object in an array and set it to rowData
      setRowData(prevRowData => [...prevRowData, user]);
    }
  }, [user]);

  // Container: Defines the grid's theme & dimensions.
  return (
    <div style={{ height: '90%', width: '100%', overflowX: 'auto' }} className="ag-theme-alpine">
        <AgGridReact 
            rowData={rowData} 
            columnDefs={columnDefs} 
            defaultColDef={{
              headerClass: 'center-header',
              cellClass: 'center-cell',
              resizable: true,
              sortable: false,
              minWidth: 150
            }}
            domLayout="normal"
            components={{ customTooltip: CustomTooltip }}
            tooltipShowDelay={0}
        />
    </div>
  );
};

export default DataTable;