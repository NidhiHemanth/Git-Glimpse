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
            response = requests.get("https://api.github.com/users/{}/{}".format(username, api), headers=HEADERS)
            for user in response.json():
                new_usernames_set.add(user["login"])

    with open("usernames.txt", "a") as file:
        file.truncate(0)
        file.write("\n".join(new_usernames_set))
    
    return new_usernames_set

def getProjectData(username: str, api_url: str) -> dict:
    api_url = api_url.split("{")[0]

    # response.json() is a list of projects
    response = requests.get(api_url, headers=HEADERS)

    # pick the 5 projects having the most stars
    top_starred_project = sorted(response.json(), key = lambda x: x["stargazers_count"], reverse=True)[:1]

    projects = []
    for project in top_starred_project:
        project_object = SAMPLE_PROJECT.copy()
        project_object["projectName"] = project["full_name"]
        project_object["description"] = project["description"]
        project_object["language"] = project["language"]
        project_object["stargazersCount"] = project["stargazers_count"]
        project_object["forksCount"] = project["forks_count"]
        project_object["htmlUrl"] = project["html_url"]
        projects.append(project_object)

    return projects[0] if len(projects) > 0 else {}

def getUserData(username: str) -> str:
    user_object = SAMPLE_USER.copy()

    response = requests.get("https://api.github.com/users/{}".format(username), headers=HEADERS)
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
