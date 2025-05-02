import { useState, useEffect } from 'react';
import { GithubUser } from '../types/GithubUser';
import apiClient from '../axios'; 
import { handleApiError } from './handleErrors';

// Fetch a GitHub user by their username
export const fetchGithubUser = async (username: string): Promise<GithubUser> => {
  try {
    const response = await apiClient.get(`/user/${username}`);
    return response.data;
  } catch (error: any) {
    handleApiError(error)
    throw error;
  }
};

// Custom hook to fetch a single GitHub user by their username
export const useFetchGithubUser = (username: string) => {
  const [user, setUser] = useState<GithubUser | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchUserData = async () => {
      try {
        const data = await fetchGithubUser(username);
        setUser(data);
      } catch (error) {
        console.error('Error fetching GitHub user:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchUserData();
  }, [username]); // Re-run effect if username changes

  return { user, loading };
};