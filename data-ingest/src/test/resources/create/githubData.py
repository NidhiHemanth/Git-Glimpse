import requests
import json

SAMPLE_USER = {}
SAMPLE_PROJECT = {}
HEADERS = ""

def scrapeFollowersAndFollowing(usernames: list) -> list:
    new_usernames_set = set(usernames)
    api_lists = ["followers","following"]

    for username in usernames:
        for api in api_lists:
            response = requests.get(f"https://api.github.com/users/{username}/{api}", headers=HEADERS)
            for user in response.json():
                new_usernames_set.add(user["login"])

    with open("usernames.txt", "a") as file:
        file.truncate(0)
        file.write("\n".join(new_usernames_set))
    
    return new_usernames_set

def getProjectData(username: str, api_url: str) -> dict:
    api_url = api_url.split("{")[0]

    page = 1
    all_projects = []

    while True:
        url = f"{api_url}?per_page=100&page={page}"
        response = requests.get(url, headers=HEADERS)

        # response.json() is a list of projects
        repos = response.json()
    
        if not repos:
            break

        all_projects.extend(repos)
        page += 1

    print(f">> Total projects for {username} is {len(all_projects)}")
    if not all_projects:
        return {}

    # Pick the project with the most stars
    top_starred_project = max(all_projects, key=lambda x: x["stargazers_count"])

    project_object = SAMPLE_PROJECT.copy()
    project_object["projectName"] = top_starred_project["full_name"]
    project_object["description"] = top_starred_project["description"]
    project_object["language"] = top_starred_project["language"]
    project_object["stargazersCount"] = top_starred_project["stargazers_count"]
    project_object["forksCount"] = top_starred_project["forks_count"]
    project_object["htmlUrl"] = top_starred_project["html_url"]

    return project_object

def getUserData(username: str) -> str:
    user_object = SAMPLE_USER.copy()

    response = requests.get(f"https://api.github.com/users/{username}", headers=HEADERS)
    if response.status_code != 200:
        print("ERR: {} not found".format(username))

    user_object["username"] = username
    user_object["fullName"] = response.json()["name"]
    user_object["email"] = response.json()["email"]
    user_object["bio"] = response.json()["bio"]
    user_object["followers"] = response.json()["followers"]
    user_object["following"] = response.json()["following"]
    user_object["profileUrl"] = response.json()["html_url"]
    user_object["avatarUrl"] = response.json()["avatar_url"]
    user_object["mostStarredProject"] = getProjectData(username, response.json()["repos_url"])

    print(f"User object for {username} added")
    return user_object

if __name__ == '__main__':

    # extract list of usernames
    usernames = []
    with open("usernames.txt") as file:
        text = file.read()
        usernames = text.split()

    personal_access_token = input("Input your Personal Access Token: ")
    HEADERS = { 'Authorization': f"Bearer {personal_access_token}" }

    # TODO: remove when enough usernames collected
    new_usernames = scrapeFollowersAndFollowing(usernames)
    usernames = list(new_usernames)

    # create sample JSON object for a user
    with open("sample.json") as sample:
        SAMPLE_USER = json.loads(sample.read())
        SAMPLE_PROJECT = SAMPLE_USER["mostStarredProject"]

    # store each user's data into a JSON array
    with open("response.json", "a") as file:
        file.truncate(0)
        file.write("[\n")
        usernames_length = len(usernames)
        for i in range(usernames_length):
            last_char = ",\n"
            if i == usernames_length-1:
                last_char = "\n"
            file.write(json.dumps(getUserData(usernames[i])) + last_char)
        file.write("]")
