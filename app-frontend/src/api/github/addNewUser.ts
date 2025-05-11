import apiClient from "../axios";
import { defaultHeaders } from "./getHeader";

export const addNewUser = async (username: String): Promise<any> => {
  try {
    const response = await apiClient.get(
      `/user/add/${username}`, 
      { headers: defaultHeaders }
    );
    return `User ${username} added successfully.`
  } catch (error: any) {
    if (error.response && error.response.status === 409) {
      return `User ${username} already exists in database.`;
    } else if (error.response && error.response.status === 404) {
      return `Username ${username} is invalid.`;
    } else {
      console.log(`ERROR while adding user ${username}: `, error)
      return `Unexpected error response from server.`;
    }
  }
}
