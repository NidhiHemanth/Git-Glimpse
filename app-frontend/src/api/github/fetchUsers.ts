import apiClient from "../axios";
import { FetchRequest, FetchResponse } from "../types/FetchRequest";
import { handleApiError } from "./handleErrors";
import { defaultHeaders } from "./getHeader";

export const fetchUsers = async (request: FetchRequest): Promise<FetchResponse> => {
  try {
    const response = await apiClient.post(
      '/fetch', 
      request, 
      { headers: defaultHeaders }
    );
    return response.data;
  } catch (error: any) {
    handleApiError(error)
    throw error;
  }
}
