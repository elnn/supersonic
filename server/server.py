# -*- coding: utf-8 -*-

import sqlite3
import json
from flask import Flask, g, request, redirect, url_for


# DEBUG = True
DATABASE = 'sonic.db'
SECRET_KEY = '12ij0asdfjqpo5109ijaeojrtOIRJPEt0195'
USERNAME = 'admin'
PASSWORD = 'sonic'

app = Flask(__name__)
app.config.from_object(__name__)


def connect_db():
    return sqlite3.connect(app.config['DATABASE'])


def init_db():
    with closing(connect_db()) as db:
        with app.open_resource('schema.sql', mode='r') as f:
            db.cursor().executescript(f.read())
            db.commit()


@app.before_request
def before_request():
    g.db = connect_db()
    g.db.row_factory = sqlite3.Row


@app.teardown_request
def teardown_request(exception):
    db = getattr(g, 'db', None)
    if db is not None:
        db.close()


def get_user_id(uuid):
    c = g.db.cursor()
    c.execute('SELECT * FROM sonic_user WHERE uuid=?', (uuid, ))
    item = c.fetchone()
    c.close()

    if item is None:
        return None
    return item['id']


def insert_new_user(uuid):
    if get_user_id(uuid) is None:
        c = g.db.cursor()
        c.execute('INSERT INTO sonic_user (uuid, extra) VALUES (?, "{}")',
                  (uuid, ))
        g.db.commit()
        c.close()

    return get_user_id(uuid)


def get_uuid_by_user(user_id):
    c = g.db.cursor()
    c.execute('SELECT uuid FROM sonic_user WHERE id=?', (user_id, ))
    item = c.fetchone()
    c.close()

    return item['uuid']


def get_info_by_user(user_id):
    c = g.db.cursor()
    c.execute('SELECT extra FROM sonic_user WHERE id=?', (user_id, ))
    item = c.fetchone()
    c.close()

    return json.loads(item['extra'])


def set_info_by_uuid(uuid, info):
    user_id = get_user_id(uuid)
    old_info = get_info_by_user(user_id)
    new_info = dict(old_info.items() + info.items())

    c = g.db.cursor()
    c.execute('UPDATE sonic_user SET extra=? WHERE id=?',
              (json.dumps(new_info), user_id))
    g.db.commit()
    c.close()

    return user_id


@app.route('/register/<uuid>')
def register(uuid):
    user_id = insert_new_user(uuid)

    return json.dumps({
        'user_id': user_id,
        'uuid': uuid
    })


@app.route('/user/<int:user_id>')
def get_user_by_id(user_id):
    return json.dumps({
        'user_id': user_id,
        'user_info': get_info_by_user(user_id)
    })


@app.route('/set_extra/<uuid>', methods=['POST'])
def set_info(uuid):
    info = json.loads(request.form['info'])
    user_id = set_info_by_uuid(uuid, info)

    return json.dumps({
        'user_id': user_id,
        'uuid': uuid,
        'user_info': get_info_by_user(user_id)
    })


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=9240)
