import axios from "axios";

export const handleApiError = (error: unknown): never => {
  if (axios.isAxiosError(error)) {
    console.log("Raw Error:", error);
    if (error.response) {
      console.log(`ERROR STATUS: ${error.response.status}`);
      switch (error.response.status) {
        case 404:
          throw new Error("User not found");
        case 409:
          throw new Error("User already exists");
        case 500:
          throw new Error("Server error. Please try again later.");
        default:
          throw new Error(`Unexpected error: ${error.response.status}`);
      }
    } else if (error.request) {
      throw new Error("No response from server. Check your network.");
    } else {
      throw new Error(`Unexpected error: ${error.message}`);
    }
  } else {
    throw new Error("Unknown error occurred");
  }
};
