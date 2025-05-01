export interface GithubUser {
    username: string;
    fullName?: string | null;
    email?: string | null;
    bio?: string | null;
    followers: number;
    following: number;
    mostStarredProjectName?: string | null;
    projectUrl?: string | null;
    profileUrl: string;
    avatarUrl: string;
}
  