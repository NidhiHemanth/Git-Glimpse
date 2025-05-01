import { ColDef } from "ag-grid-community";
import { GithubUser } from "./GithubUser";

// Column Definitions: Defines & controls grid columns.
export const columnDefs: ColDef<GithubUser>[] = [
  { 
    field: "username", 
    headerName: "Username", 
    minWidth: 120,
    sortable: true,
    filter: true,
    // cellRenderer: usernameRenderer,
  },
  { 
    field: "fullName", 
    headerName: "Full Name", 
    minWidth: 150,
    sortable: true,
    filter: true,
    // cellRenderer: projectNameRenderer,
  },
  { 
    field: "email", 
    headerName: "Email", 
    flex: 1, 
    minWidth: 200,
    filter: true,
    tooltipField: 'email'
  },
  { 
    field: "bio", 
    headerName: "Bio", 
    flex: 2, 
    minWidth: 250,
    tooltipField: 'bio'
  },
  { 
    field: "followers", 
    headerName: "Followers", 
    flex: 1, 
    minWidth: 100,
    sortable: true,
    filter: true,
  },
  { 
    field: "following", 
    headerName: "Following", 
    flex: 1, 
    minWidth: 100,
    sortable: true,
    filter: true,
  },
  {
    field: "mostStarredProjectName",
    headerName: "Top Project",
    flex: 1,
    minWidth: 200,
    tooltipField: 'mostStarredProjectName'
  },
  { 
    field: "avatarUrl", 
    headerName: "Avatar URL", 
    flex: 1, 
    minWidth: 250,
  }
];
