import { useState, useEffect } from 'react';
import { GithubUser } from '../types/GithubUser';
import apiClient from '../axios'; 
import { handleApiError } from './handleErrors';

export const findGithubUser = async (username: String): Promise<GithubUser | null> => {
  try {
    const response = await apiClient.get(`/user/${username}`);
    return response.data;
  } catch (error: any) {
    return null;
  }
};
