from flask import Flask, render_template, request, jsonify, Response
import os
from flask_bcrypt import Bcrypt
from flask_jwt_extended import create_access_token, JWTManager, jwt_required, get_jwt_identity, get_raw_jwt
from datetime import datetime, timedelta
from extension import db, jwt, bcrypt
from ProjektKlasser import *

if 'NAMESPACE' in os.environ and os.environ['NAMESPACE'] == 'heroku':
    db_uri = os.environ['DATABASE_URL']
    debug_flag = False
else:  # when running locally: use sqlite
    db_uri = 'sqlite:///test.db'
    debug_flag = True


def create_app():
    app = Flask(__name__)

    app.config["SQLALCHEMY_DATABASE_URI"] = db_uri
    app.debug = True

    app.config["JWT_SECRET_KEY"] = "vi ba slängde in den"
    app.config["JWT_ACCESS_TOKEN_EXPIRES"] = timedelta(minutes=10080)
    app.config['JWT_BLACKLIST_ENABLED'] = True
    app.config['JWT_BLACKLIST_TOKEN_CHECKS'] = ['access', 'refresh']
    bcrypt.init_app(app)
    db.init_app(app)
    jwt.init_app(app)

    register_endpoints(app)

    return app


@jwt.user_identity_loader
def user_identity_lookup(name):
    return name


@jwt.token_in_blacklist_loader
def check_if_token_in_blacklist(decrypted_token):
    jti = decrypted_token['jti']
    check = Blacklist.query.filter_by(jti=jti).first()
    if check is None:
        return False
    return True


def register_endpoints(app):
    @app.before_first_request
    def setup_sqlalchemy():
        db.drop_all()
        db.create_all()

    """ login/register """


    @app.route('/user', methods=['POST'])
    def register():
        """
        This function takes information from json to register a user.
        """
        username = request.get_json()["username"]
        email = request.get_json()["email"]
        profile_picture_id = request.get_json()["profile_picture_id"]
        user = User.query.filter_by(name=username).first()
        if user is not None:
            print("here")
            return jsonify({"msg": "User already exists!"}), 404
        password = request.get_json()["password"]
        user = User(username, email, password, profile_picture_id)
        db.session.add(user)
        db.session.commit()
        return Response(status=200)

    @app.route('/user/login', methods=['POST'])
    def login():

        """
        This function checks information from json to login with a registered
        user. This function will send a token if success.
        """

        username = request.get_json()["username"]
        user = User.query.filter_by(name=username).first()
        password = request.get_json()["password"]
        if user is not None:
            if bcrypt.check_password_hash(user.password, password):
                return jsonify({'access_token': create_access_token(identity=user.id)})
        else:
            return Response(status=404)

    @app.route('/user/logout', methods=['POST'])
    @jwt_required
    def logout():
        """
        This function will logout a user.
        """
        jti = get_raw_jwt()['jti']
        blacklist = Blacklist(jti)
        db.session.add(blacklist)
        db.session.commit()
        return Response(status=200)

    @app.route('/active', methods=['POST'])
    @jwt_required
    def active():
        return jsonify(result={'active': "true"})


    """ EVENT """

    @app.route('/make/event', methods=['POST'])
    @jwt_required
    def event_maker():
        """
        This function takes information from json to make a event.
        It uses information as the name of the event, whos hosting etc.
        """
        name = request.get_json()["name"]
        description = request.get_json()["description"]
        start_date = request.get_json()["start_date"]
        time = request.get_json()["time"]
        image_id = request.get_json()["image_id"]
        location = request.get_json()["location"]
        host_id = get_jwt_identity()

        new_event = Event(name, time, host_id, start_date, description, image_id, location)
        db.session.add(new_event)
        db.session.commit()
        return Response(status=200)

    @app.route('/event/all', methods=['GET'])
    def show_all_events():
        """
        This function returnes all the events created.
        """
        events = Event.query.all()
        event_list = [x.to_dict() for x in events]
        return jsonify(event_list)

    @app.route('/event/accept', methods=['POST'])
    @jwt_required
    def accept_event():
        """
        This function will apply a logged in user to a specified events attendance.
        """
        attendant_id = get_jwt_identity()
        user = User.query.filter_by(id=attendant_id).first()
        id = request.get_json()['id']
        attended_event = Event.query.filter_by(id=id).first()
        attendance = attended_event.attendance
        for us in attendance:
            if us.id == attendant_id:
                return Response(status=200)
        attendance.append(user)
        db.session.commit()
        return Response(status=200)

    @app.route('/event/delete', methods=['POST'])
    @jwt_required
    def delete_attending():
        """
        This function will take a user out of a specified events attendance.
        """
        attendant_id = get_jwt_identity()
        id = request.get_json()['id']
        attended_event = Event.query.filter_by(id=id).first()
        attendance = attended_event.attendance

        new_list = []
        for user in attendance:
            if not attendant_id == user.id:
                del_user = User.query.filter_by(id=user.id).first()
                new_list.append(del_user)


        attended_event.attendance = new_list
        db.session.add(attended_event)
        db.session.commit()
        return Response(status=200)

    @app.route('/event/check', methods=['POST'])
    @jwt_required
    def check_attending():
        """
        This function checks whether or not the user is attendning or not to
        the specified event
        """
        check = False
        user_ids = get_jwt_identity()
        event_id = request.get_json()['id']
        attended_event = Event.query.filter_by(id=event_id).first()
        attendance = attended_event.attendance
        for ids in attendance:
            if user_ids == ids.id:
                check = True
        if check == True:
            return {"check": "true"}
        else:
            return {"check": "false"}

    @app.route('/get/event/<id>', methods=['GET'])
    @jwt_required
    def get_event(id):
        """
        This function returnes a specified event
        """
        attended_event = Event.query.filter_by(id=id).first()
        return jsonify(attended_event.to_dict())


    @app.route('/get/events/created/<user_id>', methods=['GET'])
    @jwt_required
    def get_events_created(user_id):
        """
        This function will return the events this user has created
        """
        events = Event.query.filter_by(host_id=user_id).all()
        return jsonify([x.to_dict() for x in events])



    @app.route('/get/attendance/<event_id>', methods=['GET'])
    @jwt_required
    def get_attendance(event_id):
        """
        This function will return the specified events attendance
        """
        attended_event = Event.query.filter_by(id=event_id).first()
        new_list = attended_event.get_list()
        return jsonify(new_list)

    @app.route('/my/attendings/<user_id>', methods=['GET'])
    @jwt_required
    def my_attendings(user_id):
        """
        This function will return the events the user is attendning
        """
        my_events = Event.query.join(association_table).filter(association_table.c.User == user_id).all()

        return jsonify([x.to_dict() for x in my_events])

    """ USER """

    @app.route('/get/profile', methods=['GET'])
    @jwt_required
    def get_profile():
        """
        This function will return the jwt user.
        """
        user_id = get_jwt_identity()
        user = User.query.filter_by(id=user_id).first()
        return jsonify([user.to_dict()])

    @app.route('/change/profile_picture_id', methods=['POST'])
    @jwt_required
    def get_change_profile_id():
        """
        This function will exchange the current profile picture with a specified
        new one.
        """
        new_profile_id = request.get_json()['profile_picture_id']
        user_id = get_jwt_identity()
        user = User.query.filter_by(id=user_id).first()
        user.profile_picture_id = new_profile_id
        db.session.commit()
        return Response(status=200)

    @app.route('/user/all', methods=['GET'])
    def all_users():
        """
        This function will return all the users
        """
        users = User.query.all()
        user_list = [x.to_dict() for x in users]
        return jsonify(result=user_list)

    """ COMMENTS """

    @app.route('/get/comments/<event_id>', methods=['GET'])
    @jwt_required
    def get_comments_web(event_id):
        """
        This function will return a specified events comments.
        """
        event = Event.query.filter_by(id=event_id).first()
        comments = event.comments
        return jsonify([x.to_dict() for x in comments])


    @app.route('/add/comment', methods=['POST'])
    @jwt_required
    def add_comment():
        """
        This function will add a comment to a event.
        """
        user_id = get_jwt_identity()
        user = User.query.filter_by(id=user_id).first()
        id = request.get_json()['id']
        event = Event.query.filter_by(id=id).first()
        content = request.get_json()['content']

        new_comment = Comments(content, user_id)
        db.session.add(new_comment)

        event_comments = event.comments
        event_comments.append(new_comment)
        db.session.commit()
        return Response(status=200)

    @app.route('/get/all/comments', methods=['GET'])
    def get_all_comments():
        """
        This function returns all the comments made.
        """
        comments = Comments.query.all()
        return jsonify([x.to_dict() for x in comments])

    """ FOLLOWING """

    @app.route('/add/friend', methods=['POST'])
    @jwt_required
    def add_friend():
        """
        This function will make a jwt user friend with a specified user.
        """

        user_id = get_jwt_identity()
        friend_id = request.get_json()['id']
        friends = Friends.query.filter_by(friend_id=friend_id).all()
        for fr in friends:
            if user_id == fr.user_id:
                return Response(status=200)
        friend = Friends(user_id, friend_id)
        db.session.add(friend)
        db.session.commit()
        return Response(status=200)

    @app.route('/unfriend/friend', methods=['POST'])
    @jwt_required
    def del_friend():
        """
        This function will remove a follow relatioinship between jwt user and
        a specified user.
        """
        user_ids = get_jwt_identity()
        friend_ids = request.get_json()['id']
        friends = Friends.query.filter_by(friend_id=friend_ids).all()
        for fr in friends:
            if user_ids == fr.user_id:
                db.session.delete(fr)
        db.session.commit()
        return Response(status=200)


    @app.route('/get/friends/<user_id>', methods=['GET'])
    def get_friends(user_id):
        """
        This function will return every follower a specified user has.
        """
        friends_id_list = []
        friends_list = []
        friend_ids = Friends.query.filter(Friends.user_id == user_id).all()
        for friend in friend_ids:
            friends_id = friend.friend_id
            friends_id_list.append(friends_id)

        for ids in friends_id_list:
            user = User.query.filter_by(id=ids).first()
            friends_list.append(user)

        return jsonify([x.to_dict() for x in friends_list])

    @app.route('/check/friend', methods=['POST'])
    @jwt_required
    def check_friend():
        """
        This function will check if jwt user is followed by a specified user
        """
        check = False
        user_ids = get_jwt_identity()
        friend_ids = request.get_json()['id']
        friends = Friends.query.filter_by(friend_id=friend_ids).all()
        for fr in friends:
            if user_ids == fr.user_id:
                check = True
        if check == True:
            return {"check": "true"}
        else:
            return {"check": "false"}



    @app.route('/get/followers/<user_id>', methods=['GET'])
    @jwt_required
    def get_followers(user_id):
        """
        This function will return the followers of a specified user.
        """
        friends_id_list = []
        friends_list = []
        friend_ids = Friends.query.filter(Friends.friend_id == user_id).all()
        for friend in friend_ids:
            friends_id = friend.friend_id
            friends_id_list.append(friends_id)

        for ids in friends_id_list:
            user = User.query.filter_by(id=ids).first()
            friends_list.append(user)

        return jsonify([x.to_dict() for x in friends_list])


    """ Count """


    @app.route('/following/<user_id>', methods=['GET'])
    @jwt_required
    def get_followings_count(user_id):
        """
        This function will return the amount of users the specified users followes.
        """
        friends_id_list = []
        friends_list = []
        friend_ids = Friends.query.filter(Friends.user_id == user_id).all()
        for friend in friend_ids:
            friends_id = friend.friend_id
            friends_id_list.append(friends_id)

        for ids in friends_id_list:
            user = User.query.filter_by(id=ids).first()
            friends_list.append(user)

        return {"amount": len(friends_list)}

    @app.route('/events/created/<user_ids>', methods=['GET'])
    @jwt_required
    def my_attendings_count(user_ids):
        """
        This function will return the amount of events created by the specified user.
        """
        count_list = []
        my_events = Event.query.filter_by(user_id=user_ids).all()
        for event in my_events:
            count_list.append(event)

        return {"amount": len(count_list)}


    @app.route('/followers/<user_id>', methods=['GET'])
    @jwt_required
    def get_followers_count(user_id):
        """
        This function will return the amount of followers the spcified user has.
        """
        friends_id_list = []
        friends_list = []
        friend_ids = Friends.query.filter(Friends.friend_id == user_id).all()
        for friend in friend_ids:
            friends_id = friend.friend_id
            friends_id_list.append(friends_id)

        for ids in friends_id_list:
            user = User.query.filter_by(id=ids).first()
            friends_list.append(user)

        return {"amount": len(friends_list)}




    @app.route('/get/attended/events/amount/<user_id>', methods=['GET'])
    @jwt_required
    def get_attended_amount(user_id):
        """
        This function will return the amount of how many events you are attending.
        """
        new_list = []
        my_events = Event.query.join(association_table).filter(association_table.c.User == user_id).all()
        for event in my_events:
            new_list.append(event)

        return {"amount": len(new_list)}











app = create_app()
