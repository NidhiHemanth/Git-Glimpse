import { ColDef } from "ag-grid-community";
import { GithubUser } from "../../api";

const usernameRenderer = (params: any) => {
  const { value, data } = params;
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
      <img
        src={data.avatarUrl}
        alt={`${value}'s avatar`}
        style={{ width: 24, height: 24, borderRadius: '50%' }}
      />
      <a href={data.profileUrl} target="_blank" rel="noopener noreferrer">
        {value}
      </a>
    </div>
  );
}

const projectNameRenderer = (params: any) => {
  const { value, data } = params;
  return (
    value &&
    <a href={data?.projectUrl} target="_blank" rel="noopener noreferrer">
      {value.substring(value.lastIndexOf("/") + 1)}
    </a>
  );
}

// Column Definitions: Defines & controls grid columns.
export const columnDefs: ColDef<GithubUser>[] = [
  {
    field: "username",
    headerName: "Username",
    minWidth: 120,
    sortable: true,
    filter: true,
    filterParams: {
      filterOptions: ["equals", "notEqual", "contains"],
      maxNumConditions: 1,
    },
    cellRenderer: usernameRenderer,
  },
  {
    field: "fullName",
    headerName: "Full Name",
    minWidth: 150,
    sortable: true,
    filter: true,
    filterParams: {
      filterOptions: ["equals", "notEqual", "contains"],
      maxNumConditions: 1,
    },
  },
  {
    field: "email",
    headerName: "Email",
    flex: 1,
    minWidth: 200,
    filter: true,
    filterParams: {
      filterOptions: ["blank", "notBlank"],
      maxNumConditions: 1,
      //buttons: ['reset', 'apply'],
    },
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
    filterParams: {
      filterOptions: ["equals", "notEqual", "greaterThan", "lessThan"],
      maxNumConditions: 1,
    },
  },
  {
    field: "following",
    headerName: "Following",
    flex: 1,
    minWidth: 100,
    sortable: true,
    filter: true,
    filterParams: {
      filterOptions: ["equals", "notEqual", "greaterThan", "lessThan"],
      maxNumConditions: 1,
    },
  },
  {
    field: "mostStarredProjectName",
    headerName: "Top Project",
    flex: 1,
    minWidth: 200,
    tooltipField: 'mostStarredProjectName',
    filter: true,
    filterParams: {
      filterOptions: ["blank", "notBlank"],
      maxNumConditions: 1,
    },
   cellRenderer: projectNameRenderer,
  },
]