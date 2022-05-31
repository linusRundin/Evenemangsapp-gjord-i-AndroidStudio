from flask_sqlalchemy import SQLAlchemy
from datetime import datetime, timedelta
from flask_jwt_extended import create_access_token, JWTManager, jwt_required, get_jwt_identity, get_raw_jwt
from extension import bcrypt, jwt, db



association_table = db.Table('association_table', db.metadata,
                             db.Column('User', db.Integer, db.ForeignKey('user.id')),
                             db.Column('Event', db.Integer, db.ForeignKey('event.id')))

comments_table = db.Table('comments_table', db.metadata,
                             db.Column('Comments', db.Integer, db.ForeignKey('comments.id')),
                             db.Column('Event', db.Integer, db.ForeignKey('event.id')))

event_rating_table = db.Table('event_rating_table', db.metadata,
                                                          db.Column('User', db.Integer, db.ForeignKey('user.id')),
                                                          db.Column('Event', db.Integer, db.ForeignKey('event.id')))




class Friends(db.Model):
    __tablename__ = "friends"
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('user.id'), nullable=False)
    friend_id = db.Column(db.Integer, db.ForeignKey('user.id'), nullable=False)

    def __init__(self, user_id, friend_id):
        self.friend_id = friend_id
        self.user_id = user_id

    def id_to_user(self):
        return


class Comments(db.Model):
    __tablename__ = "comments"
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('user.id'),
        nullable=False)
    content = db.Column(db.String(100), unique=False, nullable=False)


    def __init__(self, content, user_id):
        self.content = content
        self.user_id = user_id

    def to_dict(self):
        user = User.query.join(Comments).filter(User.id==self.user_id).first()
        if user == None:
            return {}
        return {
                "user": user.name,
                "user_pic_id": user.profile_picture_id,
                "content": self.content}

class User(db.Model):
    __tablename__ = "user"
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(100), unique=False, nullable=False)
    email = db.Column(db.String(150), unique=True, nullable=False)
    password = db.Column(db.String(100), unique=False, nullable=False)
    host = db.relationship("Event", backref="user", lazy = "dynamic")
    comments = db.relationship("Comments", backref="user", lazy = "dynamic")
    profile_picture_id = db.Column(db.String(100), unique=False, nullable=False)
    



    def __init__(self, name, email, password, profile_picture_id):
        self.name = name
        self.email = email
        self.password = bcrypt.generate_password_hash(password).decode("utf-8")
        self.profile_picture_id = profile_picture_id



    def to_dict(self):

        return {
                "id": self.id,
                "name": self.name,
                "email": self.email,
                "profile_picture_id": self.profile_picture_id

                }

    def get_user_friends(self):


        return

class Event(db.Model):
    __tablename__ = "event"
    id = db.Column(db.Integer, primary_key=True)
    description = db.Column(db.String(500), unique=False, nullable=True)
    date = db.Column(db.String(200), unique=False, nullable=False)
    name = db.Column(db.String(200) , unique=False, nullable=False)
    user_id = db.Column(db.Integer, db.ForeignKey('user.id'),
        nullable=False)
    image_id = db.Column(db.String(200), unique=True, nullable =False)
    time = db.Column(db.String(50), unique=False, nullable=False)
    attendance = db.relationship("User", secondary=association_table, backref=db.backref("events", lazy = "dynamic"), lazy = "dynamic")
    comments = db.relationship("Comments", secondary=comments_table, backref=db.backref("events", lazy = "dynamic"), lazy = "dynamic")
    location = db.Column(db.String(100), unique=False, nullable=False)



    def __init__(self, name, time, user_id, date, description, image_id, location):
        self.user_id = user_id
        self.name = name
        self.time = time
        self.date = date
        self.description = description
        self.image_id = image_id
        self.location = location

    def to_dict(self):
        host = User.query.join(Event).filter(User.id==self.user_id).first()
        attendance = User.query.join(association_table).filter(association_table.c.Event==self.id).all()
        comments = Comments.query.join(comments_table).filter(comments_table.c.Event==self.id).all()
        if host == None:
            name = "unidentified"
        else:
            name= host.name
        return {"name": self.name,
                "time": self.time,
                "start_date": self.date,
                "host": name,
                "description": self.description,
                "id": self.id,
                "image_id": self.image_id,
                "attendance": [x.to_dict() for x in attendance],
                "comments": [x.to_dict() for x in comments],
                "location": self.location
                }

    def get_list(self):
        attendance = User.query.join(association_table).filter(association_table.c.Event==self.id).all()
        return [x.to_dict() for x in attendance]




class Blacklist(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    jti = db.Column(db.String(36), nullable=False)
    expires = db.Column(db.DateTime, nullable=False)

    def __init__(self, jti):
        self.jti = jti
        self.expires = datetime.now()
