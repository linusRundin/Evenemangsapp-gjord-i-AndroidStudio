import os
import tempfile
import pytest
import requests

from ProjektFunktioner import app, db




@pytest.fixture
def client():
    db_fd, app.config['./test.db'] = tempfile.mkstemp()
    app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///' + app.config['./test.db']
    app.config['TESTING'] = True

    client = app.test_client()

    with app.app_context():
        db.drop_all()
        db.create_all()

    yield client

    os.close(db_fd)
    os.unlink(app.config['./test.db'])


def testLoginRegister(client):
    data = {'username': 'simon', 'password': '1234', 'email': "simonsimon@gmail.com", "profile_picture_id": "mormors_mat"}
    data2 = {'username': 'simon', 'password': '1234'}

    register = client.post("https://projekt-app.herokuapp.com/user", json=data)
    assert register.status_code == 200

    user_1 = client.post("https://projekt-app.herokuapp.com/user/login", json=data2)
    assert user_1.status_code == 200

    token = user_1.json['access_token']

    header = {"Authorization": "Bearer " + token}

    logout = client.post("https://projekt-app.herokuapp.com/user/logout", headers=header)
    assert logout.status_code == 200



def testMakeEvent(client):
    data = {'username': 'simon', 'password': '1234', 'email': "simonsimon@gmail.com", "profile_picture_id": "mormors_mat"}
    data2 = {'username': 'simon', 'password': '1234'}
    event_data = {"name": "test event", "description": "hej välkommen kompis", "start_date": "idag", "time": "tjugofyrafem",
                  "image_id": "mormors_mat", "location": "Glava"}

    client.post("https://projekt-app.herokuapp.com/user", json=data)
    user_1 = client.post("https://projekt-app.herokuapp.com/user/login", json=data2)
    token = user_1.json['access_token']
    header = {"Authorization": "Bearer " + token}

    event = client.post("https://projekt-app.herokuapp.com/make/event", headers=header, json=event_data)

    assert event.status_code == 200

    all_events = client.get("https://projekt-app.herokuapp.com/event/all", headers=header)

    assert all_events.json == [{'attendance': [], 'comments': [], 'description': 'hej välkommen kompis', 'host': 'simon',
                                'id': 1, 'image_id': 'mormors_mat', 'location': 'Glava', 'name': 'test event',
                                'start_date': 'idag', 'time': 'tjugofyrafem'}]

def testMakeComment(client):
    data = {'username': 'simon', 'password': '1234', 'email': "simonsimon@gmail.com",
            "profile_picture_id": "mormors_mat"}
    data2 = {'username': 'simon', 'password': '1234'}
    event_data = {"name": "test event", "description": "hej välkommen kompis", "start_date": "idag",
                  "time": "tjugofyrafem",
                  "image_id": "mormors_mat", "location": "Glava"}

    client.post("https://projekt-app.herokuapp.com/user", json=data)
    user_1 = client.post("https://projekt-app.herokuapp.com/user/login", json=data2)
    token = user_1.json['access_token']
    header = {"Authorization": "Bearer " + token}

    event = client.post("https://projekt-app.herokuapp.com/make/event", headers=header, json=event_data)

    all_events = client.get("https://projekt-app.herokuapp.com/event/all", headers=header)

    comment_data = {"content": "hejsimon är ful", "id": 1}

    comment = client.post("https://projekt-app.herokuapp.com/add/comment", headers=header, json=comment_data)

    assert comment.status_code == 200

    all_comments = client.get("https://projekt-app.herokuapp.com/get/all/comments", headers=header)

    assert all_comments.json == [{'content': 'hejsimon är ful', 'user': 'simon', 'user_pic_id': 'mormors_mat'}]

def testMakeFriend(client):
    data = {'username': 'simon', 'password': '1234', 'email': "simonsimon@gmail.com",
            "profile_picture_id": "mormors_mat"}
    friend_data = {'username': 'linus', 'password': '1234', 'email': "linuslinus@gmail.com",
            "profile_picture_id": "mormors_mat"}
    data2 = {'username': 'simon', 'password': '1234'}
    friend_data2 = {'username': 'linus', 'password': '1234'}

    client.post("https://projekt-app.herokuapp.com/user", json=data)
    user_1 = client.post("https://projekt-app.herokuapp.com/user/login", json=data2)
    token = user_1.json['access_token']
    header = {"Authorization": "Bearer " + token}

    client.post("https://projekt-app.herokuapp.com/user", json=friend_data)
    user_2 = client.post("https://projekt-app.herokuapp.com/user/login", json=friend_data2)
    token_2 = user_1.json['access_token']
    header_2 = {"Authorization": "Bearer " + token}

    friends = client.post("https://projekt-app.herokuapp.com/add/friend", headers=header, json={"id": 2})

    assert friends.status_code == 200

    sim_friends = client.get("https://projekt-app.herokuapp.com/get/friends/1")

    assert sim_friends.json == [{'email': 'linuslinus@gmail.com', 'id': 2, 'name': 'linus', 'profile_picture_id': 'mormors_mat'}]

    del_friends = client.post("https://projekt-app.herokuapp.com/unfriend/friend", headers=header, json={"id": 2})

    assert del_friends.status_code == 200

    sim_friends_deleted = client.get("https://projekt-app.herokuapp.com/get/friends/1")

    assert sim_friends_deleted.json == []

















