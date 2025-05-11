import { ModuleRegistry } from 'ag-grid-community';
import { ServerSideRowModelModule } from "ag-grid-enterprise";
import { TextFilterModule, TooltipModule, ColumnApiModule, ColumnAutoSizeModule, ValidationModule, CellStyleModule } from 'ag-grid-community';

export const registerAgGridModules = () => {
  ModuleRegistry.registerModules([
    ServerSideRowModelModule,
    TextFilterModule,
    TooltipModule,
    ColumnApiModule,
    ColumnAutoSizeModule, CellStyleModule,
    ValidationModule,
  ]);
};
