'use client';

import { useState, useRef, useEffect } from "react";
import { ModuleRegistry } from "ag-grid-community";
import { ServerSideRowModelModule } from "ag-grid-enterprise";
import { AgGridReact } from "ag-grid-react";
import { GithubUser } from "../api";
import { columnDefs } from "./config/ColumnDefs";
import { CustomTooltip } from "./config/CustomToolTip";
import { defaultColumnDefs } from "./config/DefaultColumnDefs";

ModuleRegistry.registerModules([ServerSideRowModelModule]);

const DataTable = () => {
  const gridRef = useRef<AgGridReact>(null);

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
            columnDefs={columnDefs}
            rowModelType={'serverSide'} 
            domLayout="normal"
            defaultColDef={defaultColumnDefs}
            components={{ customTooltip: CustomTooltip }}
            tooltipShowDelay={0}
        />
    </div>
  );
};

export default DataTable;