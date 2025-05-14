import { ModuleRegistry } from 'ag-grid-community';
import { ServerSideRowModelModule } from "ag-grid-enterprise";
import { TextFilterModule, TooltipModule, ColumnApiModule, ColumnAutoSizeModule, ValidationModule, CellStyleModule, NumberFilterModule } from 'ag-grid-community';

export const registerAgGridModules = () => {
  ModuleRegistry.registerModules([
    ServerSideRowModelModule,
    TextFilterModule,
    NumberFilterModule,
    TooltipModule,
    ColumnApiModule,
    ColumnAutoSizeModule, CellStyleModule,
    ValidationModule,
  ]);
};
