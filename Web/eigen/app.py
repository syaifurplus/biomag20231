from flask import Flask, request, jsonify, render_template, redirect, url_for, session,  send_file
import requests
import json
from datetime import datetime
import qrcode
import base64
from flask_weasyprint import HTML, render_pdf

from io import BytesIO
import werkzeug
from werkzeug.wrappers import response

app = Flask(__name__)
SECRET_KEY = "wastemanagement"
SESSION_TYPE = 'filesystem'
app.config.from_object(__name__)

# ODOO PART
import xmlrpc.client
URL = 'http://203.194.112.170:8070'
DB = 'waste'
TITLE = 'Produk Organik'

def serve_pil_image(pil_img):
    img_io = BytesIO()
    pil_img.save(img_io, 'JPEG')
    img_io.seek(0)
    return send_file(img_io, mimetype='image/jpeg')

@app.route('/')
def index():
    return render_template(
        'frontend/index.html',
        title=TITLE
    )

@app.route('/about')
def about():
    return render_template(
        'frontend/about.html',
        title=TITLE
    )

@app.route('/blog')
def blog():
    return render_template(
        'frontend/blog.html',
        title=TITLE
    )

@app.route('/contact')
def contact():
    return render_template(
        'frontend/contact.html',
        title=TITLE
    )

@app.route('/support')
def support():
    return render_template(
        'frontend/support.html',
        title=TITLE
    )

@app.route('/login', methods=['POST', 'GET'])
def login():
    err = '0'
    if request.method == 'POST':
        username = request.form['username']
        password = request.form['pasword']

        common = xmlrpc.client.ServerProxy('{}/xmlrpc/2/common'.format(URL))
        uid = common.authenticate(DB, username, password, {})
        if uid:
            models = xmlrpc.client.ServerProxy('{}/xmlrpc/2/object'.format(URL))
            userdata = models.execute_kw(DB, uid, password,
                'res.users', 'search_read',
                [[['id', '=', uid]]],
                {'fields': ['name', 'partner_id']}
            )
            ceksetor = models.execute_kw(DB, uid, password, 'setor.setor', 'check_access_rights', ['create'], {'raise_exception': False})
            cekambil = models.execute_kw(DB, uid, password, 'ambil.ambil', 'check_access_rights', ['read'], {'raise_exception': False})

            if len(userdata)>= 1:
                session['id_user'] = uid
                session['user_uid'] = userdata[0]['partner_id'][0]
                session['password'] = password
                session['username'] = username
                session['nama'] = userdata[0]['name']
                session['ceksetor'] = ceksetor
                session['cekambil'] = cekambil

                return redirect(url_for('dashboard'))
            else:
                err = '1'
        else:
            err = '9'

    return render_template(
        'backend/sign-in.html',
        title=TITLE,
        err=err
    )

@app.route("/logout")
def logout():
    session.pop('id_user', None)
    session.pop('username', None)
    session.pop('nama', None)
    session.pop('ceksetor', None)
    session.pop('cekambil', None)
    return redirect(url_for('login'))

@app.route('/dashboard', methods=['POST', 'GET'])
def dashboard():
    if 'id_user' in session:
        if session['id_user']:
            setor_draft = 0
            setor_ready = 0
            setor_proses = 0
            setor_sukses = 0
            setor_cancel = 0
            setor_x = []
            setor_y = []
            # setor =================================================
            if session['ceksetor']:
                models = xmlrpc.client.ServerProxy('{}/xmlrpc/2/object'.format(URL))
                setor = models.execute_kw(DB, session['id_user'], session['password'],
                    'setor.setor', 'search_read',
                    [[['partner_penyetor', '=', session['user_uid']]]],
                    {'fields': ['state', 'tanggal']}
                )
                listtgl_setor = []
                for line in setor:
                    if int(line['tanggal'].split('-')[1]) not in setor_x:
                        setor_x.append(int(line['tanggal'].split('-')[1]))
                    listtgl_setor.append({'tanggal': int(line['tanggal'].split('-')[1]), 'no': 1})
                    if line['state'] == 'draft':
                        setor_draft += 1
                    elif line['state'] == 'ready':
                        setor_ready += 1
                    elif line['state'] == 'proses':
                        setor_proses += 1
                    elif line['state'] == 'sukses':
                        setor_sukses += 1
                    elif line['state'] == 'cancel':
                        setor_cancel += 1
                for zz in setor_x:
                    countt = 0
                    for yy in listtgl_setor:
                        if yy['tanggal'] == zz:
                            countt += 1
                    setor_y.append(countt)

            ambil_ready = 0
            ambil_proses = 0
            ambil_sukses = 0
            ambil_cancel = 0
            ambil_x = []
            ambil_y = []
            # ambil =================================================
            if session['cekambil']:
                models = xmlrpc.client.ServerProxy('{}/xmlrpc/2/object'.format(URL))
                ambil = models.execute_kw(DB, session['id_user'], session['password'],
                    'ambil.ambil', 'search_read',
                    [[['partner_pengambil', '=', session['user_uid']]]],
                    {'fields': ['state', 'tanggal']}
                )

                listtgl_ambil = []
                for line in ambil:
                    if int(line['tanggal'].split('-')[1]) not in ambil_x:
                        ambil_x.append(int(line['tanggal'].split('-')[1]))
                    listtgl_ambil.append({'tanggal': int(line['tanggal'].split('-')[1]), 'no': 1})
                    if line['state'] == 'ready':
                        ambil_ready += 1
                    elif line['state'] == 'proses':
                        ambil_proses += 1
                    elif line['state'] == 'sukses':
                        ambil_sukses += 1
                    elif line['state'] == 'cancel':
                        ambil_cancel += 1
                for zz in ambil_x:
                    countt = 0
                    for yy in listtgl_ambil:
                        if yy['tanggal'] == zz:
                            countt += 1
                    ambil_y.append(countt)

            return render_template(
                'backend/dashboard.html',
                title=TITLE,
                setor_draft=setor_draft,
                setor_ready=setor_ready,
                setor_proses=setor_proses,
                setor_sukses=setor_sukses,
                setor_cancel=setor_cancel,
                setor_x=setor_x,
                setor_y=setor_y,
                ambil_ready=ambil_ready,
                ambil_proses=ambil_proses,
                ambil_sukses=ambil_sukses,
                ambil_cancel=ambil_cancel,
                ambil_x=ambil_x,
                ambil_y=ambil_y,
            )
        else:
            return redirect(url_for('logout'))
    else:
        return redirect(url_for('logout'))

@app.route('/setor', methods=['POST', 'GET'])
def setor():
    if 'id_user' in session:
        if session['id_user']:
            models = xmlrpc.client.ServerProxy('{}/xmlrpc/2/object'.format(URL))
            setor = models.execute_kw(DB, session['id_user'], session['password'],
                'setor.setor', 'search_read',
                [[['partner_penyetor', '=', session['user_uid']]]],
                {'order' : 'name desc'}
            )
            for line in setor:
                line['sampah_harga'] = '{0:,.0f}'.format(line['sampah_harga']) 

            return render_template(
                'backend/penyetoran.html',
                title=TITLE,
                datasetor=setor
            )
        else:
            return redirect(url_for('logout'))
    else:
        return redirect(url_for('logout'))

@app.route('/setor_input', methods=['POST', 'GET'])
def setor_input():
    if 'id_user' in session:
        if session['id_user']:
            err = 0
            models = xmlrpc.client.ServerProxy('{}/xmlrpc/2/object'.format(URL))

            if request.method == 'POST':
                partner_pengambil = request.form['partner_pengambil']
                mtd_pengambilan = request.form['mtd_pengambilan']
                jenis_sampah = request.form['jenis_sampah']
                partner_sampah = request.form['partner_sampah']
                sampah_harga = request.form['sampah_harga']
                sampah_nama = request.form['sampah_nama']
                sampah_berat = request.form['sampah_berat']
                sampah_satuan = request.form['sampah_satuan']

                vals = {
                    "name": "New",
                    "tanggal": datetime.today().strftime("%Y-%m-%d"),
                    "partner_pengambil": int(partner_pengambil),
                    "partner_penyetor": int(session['user_uid']),
                    "mtd_pengambilan": mtd_pengambilan,
                    "jenis_sampah": jenis_sampah,
                    "partner_sampah": int(partner_sampah),
                    "sampah_nama": sampah_nama,
                    "sampah_berat": float(sampah_berat),
                    "sampah_harga": float(sampah_harga),
                    "sampah_satuan": sampah_satuan,
                }

                add_ambil = models.execute_kw(DB, session['id_user'], session['password'], 'setor.setor', 'create', [vals])
                if add_ambil:
                    err = '1'
                else:
                    err = '9'

            datapengambil = models.execute_kw(DB, session['id_user'], session['password'],
                'kontrak.pengambil', 'search_read',
                [[['contact_id', '=', int(session['user_uid'])]]],
            )

            contactdata = models.execute_kw(DB, session['id_user'], session['password'],
                'res.partner', 'search_read',
                [[['active', '=', True]]],
            )
      
            return render_template(
                'backend/penyetoran_input.html',
                title=TITLE,
                datapengambil=datapengambil,
                contactdata=contactdata,
                err=err
            )
        else:
            return redirect(url_for('logout'))
    else:
        return redirect(url_for('logout'))

@app.route('/setor_edit/<string:idnya>', methods=['POST', 'GET'])
def setor_edit(idnya):
    if 'id_user' in session:
        if session['id_user']:
            err = 0
            models = xmlrpc.client.ServerProxy('{}/xmlrpc/2/object'.format(URL))

            if request.method == 'POST':
                partner_pengambil = request.form['partner_pengambil']
                mtd_pengambilan = request.form['mtd_pengambilan']
                jenis_sampah = request.form['jenis_sampah']
                partner_sampah = request.form['partner_sampah']
                sampah_harga = request.form['sampah_harga']
                sampah_nama = request.form['sampah_nama']
                sampah_berat = request.form['sampah_berat']
                sampah_satuan = request.form['sampah_satuan']

                vals = {
                    "partner_pengambil": int(partner_pengambil),
                    "mtd_pengambilan": mtd_pengambilan,
                    "jenis_sampah": jenis_sampah,
                    "partner_sampah": int(partner_sampah),
                    "sampah_nama": sampah_nama,
                    "sampah_berat": float(sampah_berat),
                    "sampah_harga": float(sampah_harga),
                    "sampah_satuan": sampah_satuan,
                }

                edit_setor = models.execute_kw(DB, session['id_user'], session['password'], 'setor.setor', 'write', [[int(idnya)], vals]) 
                if edit_setor:
                    err = '1'
                else:
                    err = '9'

            datapengambil = models.execute_kw(DB, session['id_user'], session['password'],
                'kontrak.pengambil', 'search_read',
                [[['contact_id', '=', int(session['user_uid'])]]],
            )

            contactdata = models.execute_kw(DB, session['id_user'], session['password'],
                'res.partner', 'search_read',
                [[['active', '=', True]]],
            )

            datasetor = models.execute_kw(DB, session['id_user'], session['password'],
                'setor.setor', 'search_read',
                [[['id', '=', int(idnya)]]],
            )
            for line in datasetor:
                line['sampah_harga'] = int(line['sampah_harga']) 
      
            return render_template(
                'backend/penyetoran_edit.html',
                title=TITLE,
                datapengambil=datapengambil,
                contactdata=contactdata,
                datasetor=datasetor,
                err=err
            )
        else:
            return redirect(url_for('logout'))
    else:
        return redirect(url_for('logout'))

@app.route("/setor_set_state/<string:idnya>/<string:statenya>", methods=['GET', 'POST'])
def setor_set_state(idnya, statenya):
    if 'id_user' in session:
        if session['id_user']:
            models = xmlrpc.client.ServerProxy('{}/xmlrpc/2/object'.format(URL))

            vals = {
                'state': statenya,
                'dari': 'dari'
            }

            update_ambil = models.execute_kw(DB, session['id_user'], session['password'], 'setor.setor', 'write', [[int(idnya)], vals])
            if update_ambil:
                return redirect(url_for('setor'))
        else:
            return redirect(url_for('logout'))
    else:
        return redirect(url_for('logout'))

@app.route("/setor_set_delete/<string:idnya>", methods=['GET', 'POST'])
def setor_set_delete(idnya):
    if 'id_user' in session:
        if session['id_user']:
            models = xmlrpc.client.ServerProxy('{}/xmlrpc/2/object'.format(URL))
            delete_ambil = models.execute_kw(DB, session['id_user'], session['password'], 'setor.setor', 'unlink', [[int(idnya)]])
            if delete_ambil:
                return redirect(url_for('setor'))
        else:
            return redirect(url_for('logout'))
    else:
        return redirect(url_for('logout'))

@app.route('/ambil', methods=['POST', 'GET'])
def ambil():
    if 'id_user' in session:
        if session['id_user']:

            models = xmlrpc.client.ServerProxy('{}/xmlrpc/2/object'.format(URL))
            ambil = models.execute_kw(DB, session['id_user'], session['password'],
                'ambil.ambil', 'search_read',
                [[['partner_pengambil', '=', session['user_uid']]]],
                {'order' : 'name desc'}
            )
            for line in ambil:
                line['sampah_harga'] = '{0:,.0f}'.format(line['sampah_harga']) 

            return render_template(
                'backend/pengambilan.html',
                title=TITLE,
                dataambil=ambil
            )
        else:
            return redirect(url_for('logout'))
    else:
        return redirect(url_for('logout'))

@app.route('/ambil_input', methods=['POST', 'GET'])
def ambil_input():
    if 'id_user' in session:
        if session['id_user']:
            err = 0
            models = xmlrpc.client.ServerProxy('{}/xmlrpc/2/object'.format(URL))

            if request.method == 'POST':
                tanggal = datetime.today().strftime("%Y-%m-%d")
                no_penyetoran = request.form['no_penyetoran']
                sampah_harga = request.form['sampah_harga']

                vals = {
                    "name": "New",
                    "tanggal": tanggal,
                    "partner_pengambil": int(session['user_uid']),
                    "no_penyetoran": int(no_penyetoran),
                    "sampah_harga": float(sampah_harga),
                }

                add_ambil = models.execute_kw(DB, session['id_user'], session['password'], 'ambil.ambil', 'create', [vals])
                if add_ambil:
                    err = '1'
                else:
                    err = '9'

            datasetor = models.execute_kw(DB, session['id_user'], session['password'],
                'setor.setor', 'search_read',
                [[['state', '=', 'ready'], ['partner_pengambil', '=', int(session['user_uid'])]]],
                {'order' : 'name desc'}
            )
            for line in datasetor:
                line['sampah_harga'] = int(line['sampah_harga']) 

            return render_template(
                'backend/pengambilan_input.html',
                title=TITLE,
                datasetor=datasetor,
                err=err
            )
        else:
            return redirect(url_for('logout'))
    else:
        return redirect(url_for('logout'))

@app.route('/ambil_edit/<string:idnya>', methods=['POST', 'GET'])
def ambil_edit(idnya):
    if 'id_user' in session:
        if session['id_user']:
            err = 0
            models = xmlrpc.client.ServerProxy('{}/xmlrpc/2/object'.format(URL))

            if request.method == 'POST':
                sampah_harga = request.form['sampah_harga']

                vals = {
                    "sampah_harga": float(sampah_harga),
                }

                add_ambil = models.execute_kw(DB, session['id_user'], session['password'], 'ambil.ambil', 'write', [[int(idnya)], vals])
                if add_ambil:
                    err = '1'
                else:
                    err = '9'

            ambil = models.execute_kw(DB, session['id_user'], session['password'],
                'ambil.ambil', 'search_read',
                [[['id', '=', int(idnya)]]],
                {'order' : 'name desc'}
            )
            for line in ambil:
                line['sampah_harga'] = int(line['sampah_harga']) 

            datasetor = []
            if ambil[0]['no_penyetoran']:
                datasetor = models.execute_kw(DB, session['id_user'], session['password'],
                    'setor.setor', 'search_read',
                    [[['id', '=', int(ambil[0]['no_penyetoran'][0])]]],
                    {'order' : 'name desc'}
                )
                for line in datasetor:
                    line['sampah_harga'] = int(line['sampah_harga']) 

            return render_template(
                'backend/pengambilan_edit.html',
                title=TITLE,
                datasetor=datasetor,
                dataambil=ambil,
                err=err
            )
        else:
            return redirect(url_for('logout'))
    else:
        return redirect(url_for('logout'))

@app.route("/ambil_set_state/<string:idnya>/<string:statenya>/<string:idsetor>", methods=['GET', 'POST'])
def ambil_set_state(idnya, statenya, idsetor):
    if 'id_user' in session:
        if session['id_user']:
            models = xmlrpc.client.ServerProxy('{}/xmlrpc/2/object'.format(URL))

            vals = {
                'state': statenya,
                'dari': 'dari'
            }

            update_ambil = models.execute_kw(DB, session['id_user'], session['password'], 'ambil.ambil', 'write', [[int(idnya)], vals])
            if update_ambil:
                if statenya == 'sukses':
                    vals = {
                        'state': 'sukses',
                        'dari': 'dari'
                    }
                    update_ambil = models.execute_kw(DB, session['id_user'], session['password'], 'setor.setor', 'write', [[int(idsetor)], vals])
                return redirect(url_for('ambil'))
        else:
            return redirect(url_for('logout'))
    else:
        return redirect(url_for('logout'))

@app.route("/ambil_set_delete/<string:idnya>", methods=['GET', 'POST'])
def ambil_set_delete(idnya):
    if 'id_user' in session:
        if session['id_user']:
            models = xmlrpc.client.ServerProxy('{}/xmlrpc/2/object'.format(URL))
            delete_ambil = models.execute_kw(DB, session['id_user'], session['password'], 'ambil.ambil', 'unlink', [[int(idnya)]])
            if delete_ambil:
                return redirect(url_for('ambil'))
        else:
            return redirect(url_for('logout'))
    else:
        return redirect(url_for('logout'))

@app.route('/laporan_setor', methods=['GET', 'POST'])
def laporan_setor():
    if 'id_user' in session:
        if session['id_user']:
            models = xmlrpc.client.ServerProxy('{}/xmlrpc/2/object'.format(URL))
            setor = models.execute_kw(DB, 2, 'adminwaste1234',
                'setor.setor', 
                'read_group',
                [[['tanggal', '!=', False], ['state', '!=', 'cancel'], ['partner_penyetor', '=', session['user_uid']]]],
                {
                    'fields': ['tanggal', 'sampah_nama', 'jenis_sampah', 'sampah_berat', 'sampah_harga'], 
                    'groupby' : ['tanggal']
                },
            )
            datalap = []
            datalap2 = []
            listtahun = []
            if setor:
                for line in setor:
                    tgl1 = line['__range']['tanggal']['from']
                    tgl2 = line['__range']['tanggal']['to']
                    listtahun.append(tgl1[:4])
                    datalap.append({
                        "tanggal": line['tanggal'],
                        "nama": "",
                        "jenis": "",
                        "tgl1": tgl1,
                        "tgl2": tgl2,
                        "berat": line['sampah_berat'],
                    })
                newlist = sorted(datalap, key=lambda d: d['tgl1'], reverse=True) 
                for line2 in newlist:
                    tgl1 = line2['tgl1']
                    tgl2 = line2['tgl2']
                    setorline = models.execute_kw(DB, 2, 'adminwaste1234',
                        'setor.setor', 'search_read',
                        [[['tanggal', '>=', tgl1], ['tanggal', '<', tgl2], ['partner_penyetor', '=', session['user_uid']]]],
                        {
                            'fields': ['tanggal', 'sampah_nama', 'jenis_sampah', 'sampah_berat', 'sampah_harga', 'sampah_satuan'],
                            'order' : 'tanggal desc'
                        }
                    )
                    sat = ""
                    for zz in setorline:
                        sat = zz['sampah_satuan']
                        datalap2.append({
                            "tanggal": zz['tanggal'],
                            "nama": zz['sampah_nama'],
                            "jenis": zz['jenis_sampah'],
                            "tgl1": "",
                            "tgl2": "",
                            "berat": int(zz['sampah_berat']),
                            "satuan": sat
                        })
                    datalap2.append({
                        "tanggal": "",
                        "nama": "",
                        "jenis": "Total " + line2['tanggal'],
                        "tgl1": "",
                        "tgl2": "",
                        "berat": int(line2['berat']),
                        "satuan": sat
                    })

                totalall = 0
                for zzz in newlist:
                    totalall += int(zzz['berat'])

                datalap2.append({
                    "tanggal": "",
                    "nama": "",
                    "jenis": "Total ALL",
                    "tgl1": "",
                    "tgl2": "",
                    "berat": totalall,
                    "satuan": sat
                })

            listtahun2 = []
            for zz in listtahun:
                if zz not in listtahun2:
                    listtahun2.append(zz)

            if request.method == 'POST':
                valsearch = request.form['valsearch']
                if valsearch == 'all':
                    setor = models.execute_kw(DB, 2, 'adminwaste1234',
                        'setor.setor', 
                        'read_group',
                        [[['tanggal', '!=', False], ['state', '!=', 'cancel'], ['partner_penyetor', '=', session['user_uid']]]],
                        {
                            'fields': ['tanggal', 'sampah_nama', 'jenis_sampah', 'sampah_berat', 'sampah_harga'], 
                            'groupby' : ['tanggal']
                        },
                    )
                    datalap = []
                    datalap2 = []
                    listtahun = []
                    if setor:
                        for line in setor:
                            tgl1 = line['__range']['tanggal']['from']
                            tgl2 = line['__range']['tanggal']['to']
                            listtahun.append(tgl1[:4])
                            datalap.append({
                                "tanggal": line['tanggal'],
                                "nama": "",
                                "jenis": "",
                                "tgl1": tgl1,
                                "tgl2": tgl2,
                                "berat": line['sampah_berat'],
                            })
                        newlist = sorted(datalap, key=lambda d: d['tgl1'], reverse=True) 
                        for line2 in newlist:
                            tgl1 = line2['tgl1']
                            tgl2 = line2['tgl2']
                            setorline = models.execute_kw(DB, 2, 'adminwaste1234',
                                'setor.setor', 'search_read',
                                [[['tanggal', '>=', tgl1], ['tanggal', '<', tgl2], ['partner_penyetor', '=', session['user_uid']]]],
                                {
                                    'fields': ['tanggal', 'sampah_nama', 'jenis_sampah', 'sampah_berat', 'sampah_harga', 'sampah_satuan'],
                                    'order' : 'tanggal desc'
                                }
                            )
                            sat = ""
                            for zz in setorline:
                                sat = zz['sampah_satuan']
                                datalap2.append({
                                    "tanggal": zz['tanggal'],
                                    "nama": zz['sampah_nama'],
                                    "jenis": zz['jenis_sampah'],
                                    "tgl1": "",
                                    "tgl2": "",
                                    "berat": int(zz['sampah_berat']),
                                    "satuan": sat
                                })
                            datalap2.append({
                                "tanggal": "",
                                "nama": "",
                                "jenis": "Total " + line2['tanggal'],
                                "tgl1": "",
                                "tgl2": "",
                                "berat": int(line2['berat']),
                                "satuan": sat
                            })

                        totalall = 0
                        for zzz in newlist:
                            totalall += int(zzz['berat'])

                        datalap2.append({
                            "tanggal": "",
                            "nama": "",
                            "jenis": "Total ALL",
                            "tgl1": "",
                            "tgl2": "",
                            "berat": totalall,
                            "satuan": sat
                        })

                    listtahun2 = []
                    for zz in listtahun:
                        if zz not in listtahun2:
                            listtahun2.append(zz)
                else:
                    setor = models.execute_kw(DB, 2, 'adminwaste1234',
                        'setor.setor', 
                        'read_group',
                        [[['tanggal', '!=', False], ['state', '!=', 'cancel'], ['partner_penyetor', '=', session['user_uid']]]],
                        {
                            'fields': ['tanggal', 'sampah_nama', 'jenis_sampah', 'sampah_berat', 'sampah_harga'], 
                            'groupby' : ['tanggal']
                        },
                    )
                    datalap = []
                    datalap2 = []
                    listtahun = []
                    if setor:
                        for line in setor:
                            tgl1 = line['__range']['tanggal']['from']
                            tgl2 = line['__range']['tanggal']['to']
                            listtahun.append(tgl1[:4])
                            if tgl1[:4] == valsearch:
                                datalap.append({
                                    "tanggal": line['tanggal'],
                                    "nama": "",
                                    "jenis": "",
                                    "tgl1": tgl1,
                                    "tgl2": tgl2,
                                    "berat": line['sampah_berat'],
                                })
                        newlist = sorted(datalap, key=lambda d: d['tgl1'], reverse=True) 
                        for line2 in newlist:
                            tgl1 = line2['tgl1']
                            tgl2 = line2['tgl2']
                            setorline = models.execute_kw(DB, 2, 'adminwaste1234',
                                'setor.setor', 'search_read',
                                [[['tanggal', '>=', tgl1], ['tanggal', '<', tgl2], ['partner_penyetor', '=', session['user_uid']]]],
                                {
                                    'fields': ['tanggal', 'sampah_nama', 'jenis_sampah', 'sampah_berat', 'sampah_harga', 'sampah_satuan'],
                                    'order' : 'tanggal desc'
                                }
                            )
                            sat = ""
                            for zz in setorline:
                                sat = zz['sampah_satuan']
                                datalap2.append({
                                    "tanggal": zz['tanggal'],
                                    "nama": zz['sampah_nama'],
                                    "jenis": zz['jenis_sampah'],
                                    "tgl1": "",
                                    "tgl2": "",
                                    "berat": int(zz['sampah_berat']),
                                    "satuan": sat
                                })
                            datalap2.append({
                                "tanggal": "",
                                "nama": "",
                                "jenis": "Total " + line2['tanggal'],
                                "tgl1": "",
                                "tgl2": "",
                                "berat": int(line2['berat']),
                                "satuan": sat
                            })

                        totalall = 0
                        for zzz in newlist:
                            totalall += int(zzz['berat'])

                        datalap2.append({
                            "tanggal": "",
                            "nama": "",
                            "jenis": "Total ALL",
                            "tgl1": "",
                            "tgl2": "",
                            "berat": totalall,
                            "satuan": sat
                        })

                    listtahun2 = []
                    for zz in listtahun:
                        if zz not in listtahun2:
                            listtahun2.append(zz)
            return render_template(
                'backend/laporan_setor.html',
                title=TITLE,
                datalap2=datalap2,
                listtahun2=listtahun2
            )
        else:
            return redirect(url_for('logout'))
    else:
        return redirect(url_for('logout'))

@app.route('/laporan_ambil', methods=['GET', 'POST'])
def laporan_ambil():
    if 'id_user' in session:
        if session['id_user']:
            models = xmlrpc.client.ServerProxy('{}/xmlrpc/2/object'.format(URL))
            setor = models.execute_kw(DB, 2, 'adminwaste1234',
                'ambil.ambil', 
                'read_group',
                [[['tanggal', '!=', False], ['state', '!=', 'cancel'], ['partner_pengambil', '=', session['user_uid']]]],
                {
                    'fields': ['tanggal', 'sampah_nama', 'jenis_sampah', 'sampah_berat', 'sampah_harga'], 
                    'groupby' : ['tanggal']
                },
            )
            datalap = []
            datalap2 = []
            listtahun = []
            if setor:
                for line in setor:
                    tgl1 = line['__range']['tanggal']['from']
                    tgl2 = line['__range']['tanggal']['to']
                    listtahun.append(tgl1[:4])
                    datalap.append({
                        "tanggal": line['tanggal'],
                        "nama": "",
                        "jenis": "",
                        "tgl1": tgl1,
                        "tgl2": tgl2,
                        "berat": line['sampah_berat'],
                    })
                newlist = sorted(datalap, key=lambda d: d['tgl1'], reverse=True) 
                for line2 in newlist:
                    tgl1 = line2['tgl1']
                    tgl2 = line2['tgl2']
                    setorline = models.execute_kw(DB, 2, 'adminwaste1234',
                        'ambil.ambil', 'search_read',
                        [[['tanggal', '>=', tgl1], ['tanggal', '<', tgl2], ['partner_pengambil', '=', session['user_uid']]]],
                        {
                            'fields': ['tanggal', 'sampah_nama', 'jenis_sampah', 'sampah_berat', 'sampah_harga', 'sampah_satuan'],
                            'order' : 'tanggal desc'
                        }
                    )
                    sat = ""
                    for zz in setorline:
                        sat = zz['sampah_satuan']
                        datalap2.append({
                            "tanggal": zz['tanggal'],
                            "nama": zz['sampah_nama'],
                            "jenis": zz['jenis_sampah'],
                            "tgl1": "",
                            "tgl2": "",
                            "berat": int(zz['sampah_berat']),
                            "satuan": sat
                        })
                    datalap2.append({
                        "tanggal": "",
                        "nama": "",
                        "jenis": "Total " + line2['tanggal'],
                        "tgl1": "",
                        "tgl2": "",
                        "berat": int(line2['berat']),
                        "satuan": sat
                    })

                totalall = 0
                for zzz in newlist:
                    totalall += int(zzz['berat'])

                datalap2.append({
                    "tanggal": "",
                    "nama": "",
                    "jenis": "Total ALL",
                    "tgl1": "",
                    "tgl2": "",
                    "berat": totalall,
                    "satuan": sat
                })

            listtahun2 = []
            for zz in listtahun:
                if zz not in listtahun2:
                    listtahun2.append(zz)

            if request.method == 'POST':
                valsearch = request.form['valsearch']
                if valsearch == 'all':
                    setor = models.execute_kw(DB, 2, 'adminwaste1234',
                        'ambil.ambil', 
                        'read_group',
                        [[['tanggal', '!=', False], ['state', '!=', 'cancel'], ['partner_pengambil', '=', session['user_uid']]]],
                        {
                            'fields': ['tanggal', 'sampah_nama', 'jenis_sampah', 'sampah_berat', 'sampah_harga'], 
                            'groupby' : ['tanggal']
                        },
                    )
                    datalap = []
                    datalap2 = []
                    listtahun = []
                    if setor:
                        for line in setor:
                            tgl1 = line['__range']['tanggal']['from']
                            tgl2 = line['__range']['tanggal']['to']
                            listtahun.append(tgl1[:4])
                            datalap.append({
                                "tanggal": line['tanggal'],
                                "nama": "",
                                "jenis": "",
                                "tgl1": tgl1,
                                "tgl2": tgl2,
                                "berat": line['sampah_berat'],
                            })
                        newlist = sorted(datalap, key=lambda d: d['tgl1'], reverse=True) 
                        for line2 in newlist:
                            tgl1 = line2['tgl1']
                            tgl2 = line2['tgl2']
                            setorline = models.execute_kw(DB, 2, 'adminwaste1234',
                                'ambil.ambil', 'search_read',
                                [[['tanggal', '>=', tgl1], ['tanggal', '<', tgl2], ['partner_pengambil', '=', session['user_uid']]]],
                                {
                                    'fields': ['tanggal', 'sampah_nama', 'jenis_sampah', 'sampah_berat', 'sampah_harga', 'sampah_satuan'],
                                    'order' : 'tanggal desc'
                                }
                            )
                            sat = ""
                            for zz in setorline:
                                sat = zz['sampah_satuan']
                                datalap2.append({
                                    "tanggal": zz['tanggal'],
                                    "nama": zz['sampah_nama'],
                                    "jenis": zz['jenis_sampah'],
                                    "tgl1": "",
                                    "tgl2": "",
                                    "berat": int(zz['sampah_berat']),
                                    "satuan": sat
                                })
                            datalap2.append({
                                "tanggal": "",
                                "nama": "",
                                "jenis": "Total " + line2['tanggal'],
                                "tgl1": "",
                                "tgl2": "",
                                "berat": int(line2['berat']),
                                "satuan": sat
                            })

                        totalall = 0
                        for zzz in newlist:
                            totalall += int(zzz['berat'])

                        datalap2.append({
                            "tanggal": "",
                            "nama": "",
                            "jenis": "Total ALL",
                            "tgl1": "",
                            "tgl2": "",
                            "berat": totalall,
                            "satuan": sat
                        })

                    listtahun2 = []
                    for zz in listtahun:
                        if zz not in listtahun2:
                            listtahun2.append(zz)
                else:
                    setor = models.execute_kw(DB, 2, 'adminwaste1234',
                        'ambil.ambil', 
                        'read_group',
                        [[['tanggal', '!=', False], ['state', '!=', 'cancel'], ['partner_pengambil', '=', session['user_uid']]]],
                        {
                            'fields': ['tanggal', 'sampah_nama', 'jenis_sampah', 'sampah_berat', 'sampah_harga'], 
                            'groupby' : ['tanggal']
                        },
                    )
                    datalap = []
                    datalap2 = []
                    listtahun = []
                    if setor:
                        for line in setor:
                            tgl1 = line['__range']['tanggal']['from']
                            tgl2 = line['__range']['tanggal']['to']
                            listtahun.append(tgl1[:4])
                            if tgl1[:4] == valsearch:
                                datalap.append({
                                    "tanggal": line['tanggal'],
                                    "nama": "",
                                    "jenis": "",
                                    "tgl1": tgl1,
                                    "tgl2": tgl2,
                                    "berat": line['sampah_berat'],
                                })
                        newlist = sorted(datalap, key=lambda d: d['tgl1'], reverse=True) 
                        for line2 in newlist:
                            tgl1 = line2['tgl1']
                            tgl2 = line2['tgl2']
                            setorline = models.execute_kw(DB, 2, 'adminwaste1234',
                                'ambil.ambil', 'search_read',
                                [[['tanggal', '>=', tgl1], ['tanggal', '<', tgl2], ['partner_pengambil', '=', session['user_uid']]]],
                                {
                                    'fields': ['tanggal', 'sampah_nama', 'jenis_sampah', 'sampah_berat', 'sampah_harga', 'sampah_satuan'],
                                    'order' : 'tanggal desc'
                                }
                            )
                            sat = ""
                            for zz in setorline:
                                sat = zz['sampah_satuan']
                                datalap2.append({
                                    "tanggal": zz['tanggal'],
                                    "nama": zz['sampah_nama'],
                                    "jenis": zz['jenis_sampah'],
                                    "tgl1": "",
                                    "tgl2": "",
                                    "berat": int(zz['sampah_berat']),
                                    "satuan": sat
                                })
                            datalap2.append({
                                "tanggal": "",
                                "nama": "",
                                "jenis": "Total " + line2['tanggal'],
                                "tgl1": "",
                                "tgl2": "",
                                "berat": int(line2['berat']),
                                "satuan": sat
                            })

                        totalall = 0
                        for zzz in newlist:
                            totalall += int(zzz['berat'])

                        datalap2.append({
                            "tanggal": "",
                            "nama": "",
                            "jenis": "Total ALL",
                            "tgl1": "",
                            "tgl2": "",
                            "berat": totalall,
                            "satuan": sat
                        })

                    listtahun2 = []
                    for zz in listtahun:
                        if zz not in listtahun2:
                            listtahun2.append(zz)
            return render_template(
                'backend/laporan_ambil.html',
                title=TITLE,
                datalap2=datalap2,
                listtahun2=listtahun2
            )
        else:
            return redirect(url_for('logout'))
    else:
        return redirect(url_for('logout'))

# API android =========================================================================

@app.route('/login_api', methods=['POST'])
def login_api():
    err = '0'
    result = {"result": False}
    if request.method == 'POST':
        request_data = request.get_data ()
        param = json.loads(request_data)

        username = param['username']
        password = param['pasword']

        common = xmlrpc.client.ServerProxy('{}/xmlrpc/2/common'.format(URL))
        uid = common.authenticate(DB, username, password, {})
        if uid:
            models = xmlrpc.client.ServerProxy('{}/xmlrpc/2/object'.format(URL))
            userdata = models.execute_kw(DB, uid, password,
                'res.users', 'search_read',
                [[['id', '=', uid]]],
                {'fields': ['name', 'partner_id']}
            )
            ceksetor = models.execute_kw(DB, uid, password, 'setor.setor', 'check_access_rights', ['create'], {'raise_exception': False})
            cekambil = models.execute_kw(DB, uid, password, 'ambil.ambil', 'check_access_rights', ['read'], {'raise_exception': False})

            if len(userdata)>= 1:
                result['id_user'] = uid
                result['user_uid'] = userdata[0]['partner_id'][0]
                result['password'] = password
                result['username'] = username
                result['nama'] = userdata[0]['name']
                result['otoritas_setor'] = ceksetor
                result['otoritas_ambil'] = cekambil
                result['result'] = True
    return result

@app.errorhandler(werkzeug.exceptions.BadRequest)
@app.route("/qrcode_setor/<string:data>")
def qrcode_setor(data):
    try:
        img = qrcode.make(data)
        return serve_pil_image(img)
    except Exception as e:
        print('---------------------')
        print(e)
        print('---------------------')
        return {"message":'bad request!'}, 400

@app.route('/partner_pengambil_api', methods=['POST'])
def partner_pengambil_api():
    result = {"result": False}
    if request.method == 'POST':
        request_data = request.get_data ()
        param = json.loads(request_data)

        id_user = int(param['id_user'])
        user_uid = int(param['user_uid'])
        password = param['pasword']

        models = xmlrpc.client.ServerProxy('{}/xmlrpc/2/object'.format(URL))
        datapengambil = models.execute_kw(DB, id_user, password,
            'kontrak.pengambil', 'search_read',
            [[['contact_id', '=', user_uid]]],
            {'fields': ['partner_pengambil', 'mobile']}
        )

        result = {"result": True, "data": datapengambil}

    return json.dumps(result)

@app.route('/partner_sampah_api', methods=['POST'])
def partner_sampah_api():
    result = {"result": False}
    if request.method == 'POST':
        request_data = request.get_data ()
        param = json.loads(request_data)

        id_user = int(param['id_user'])
        password = param['pasword']

        models = xmlrpc.client.ServerProxy('{}/xmlrpc/2/object'.format(URL))
        datapengambil = models.execute_kw(DB, id_user, password,
            'res.partner', 'search_read',
            [[['active', '=', True]]],
            {'fields': ['name', 'mobile']}
        )

        result = {"result": True, "data": datapengambil}

    return json.dumps(result)

@app.route('/setor_api', methods=['POST'])
def setor_api():
    result = {"result": False}
    if request.method == 'POST':
        request_data = request.get_data ()
        param = json.loads(request_data)

        offset = int(param['offset'])
        limit = int(param['limit'])
        id_user = int(param['id_user'])
        password = param['pasword']
        id_setor = param['id_setor']
        partner_id = param['partner_id']

        search = [['partner_penyetor', '=', int(partner_id)]]
        if id_setor != '%':
            search = [['id', '=', int(id_setor)], ['partner_penyetor', '=', int(partner_id)]]

        models = xmlrpc.client.ServerProxy('{}/xmlrpc/2/object'.format(URL))
        setor = models.execute_kw(DB, id_user, password,
            'setor.setor', 'search_read',
            [search],
            {'order' : 'name desc', 'offset': offset, 'limit': limit}
        )
        for line in setor:
            line['sampah_harga'] = '{0:,.0f}'.format(line['sampah_harga']) 

        result = {"result": True, "data": setor}

    return json.dumps(result)

@app.route('/setor_input_api', methods=['POST'])
def setor_input_api():
    if request.method == 'POST':
        models = xmlrpc.client.ServerProxy('{}/xmlrpc/2/object'.format(URL))
        request_data = request.get_data ()
        param = json.loads(request_data)

        id_user = int(param['id_user'])
        id_partner = int(param['partner_id'])
        password = param['pasword']

        partner_pengambil = param['partner_pengambil']
        mtd_pengambilan = param['mtd_pengambilan']
        jenis_sampah = param['jenis_sampah']
        partner_sampah = param['partner_sampah']
        sampah_harga = param['sampah_harga']
        sampah_nama = param['sampah_nama']
        sampah_berat = param['sampah_berat']
        sampah_satuan = param['sampah_satuan']
        tanggal = param['tanggal']

        vals = {
            "name": "New",
            "tanggal": tanggal,
            "partner_pengambil": int(partner_pengambil),
            "partner_penyetor": id_partner,
            "mtd_pengambilan": mtd_pengambilan,
            "jenis_sampah": jenis_sampah,
            "partner_sampah": int(partner_sampah),
            "sampah_nama": sampah_nama,
            "sampah_berat": float(sampah_berat),
            "sampah_harga": float(sampah_harga),
            "sampah_satuan": sampah_satuan,
        }

        add_ambil = models.execute_kw(DB, id_user, password, 'setor.setor', 'create', [vals])
        result = {'id_setor': add_ambil}
        return json.dumps(result)

@app.route("/setor_set_state_api", methods=['POST'])
def setor_set_state_api():
    if request.method == 'POST':
        models = xmlrpc.client.ServerProxy('{}/xmlrpc/2/object'.format(URL))
        request_data = request.get_data ()
        param = json.loads(request_data)

        id_setor = int(param['id_setor'])
        state = param['state']
        id_user = int(param['id_user'])
        password = param['pasword']

        vals = {
            'state': state,
            'dari': 'dari'
        }

        datasetor = models.execute_kw(DB, id_user, password, 'setor.setor', 'write', [[int(id_setor)], vals])
        return json.dumps(datasetor)
        
@app.route("/setor_delete_api", methods=['POST'])
def setor_delete_api():
    if request.method == 'POST':
        models = xmlrpc.client.ServerProxy('{}/xmlrpc/2/object'.format(URL))
        request_data = request.get_data ()
        param = json.loads(request_data)

        id_setor = int(param['id_setor'])
        id_user = int(param['id_user'])
        password = param['pasword']

        delete_data = models.execute_kw(DB, id_user, password, 'setor.setor', 'unlink', [[int(id_setor)]])
        return json.dumps(delete_data)

@app.route('/ambil_api', methods=['POST', 'GET'])
def ambil_api():
    result = {"result": False}
    if request.method == 'POST':
        request_data = request.get_data ()
        param = json.loads(request_data)

        offset = int(param['offset'])
        limit = int(param['limit'])
        id_user = int(param['id_user'])
        password = param['pasword']
        id_ambil = param['id_ambil']
        partner_id = param['partner_id']

        search = [['partner_pengambil', '=', int(partner_id)]]
        if id_ambil != '%':
            search = [['id', '=', int(id_ambil)], ['partner_pengambil', '=', int(partner_id)]]

        models = xmlrpc.client.ServerProxy('{}/xmlrpc/2/object'.format(URL))
        setor = models.execute_kw(DB, id_user, password,
            'ambil.ambil', 'search_read',
            [search],
            {'order' : 'name desc', 'offset': offset, 'limit': limit}
        )
        for line in setor:
            line['sampah_harga'] = '{0:,.0f}'.format(line['sampah_harga']) 

        result = {"result": True, "data": setor}

    return json.dumps(result)

@app.route("/ambil_set_state_api", methods=['POST'])
def ambil_set_state_api():
    if request.method == 'POST':
        models = xmlrpc.client.ServerProxy('{}/xmlrpc/2/object'.format(URL))
        request_data = request.get_data ()
        param = json.loads(request_data)

        id_ambil = int(param['id_ambil'])
        state = param['state']
        id_user = int(param['id_user'])
        password = param['pasword']

        vals = {
            'state': state,
            'dari': 'dari'
        }

        datasetor = models.execute_kw(DB, id_user, password, 'ambil.ambil', 'write', [[int(id_ambil)], vals])
        if state == 'sukses':
            if datasetor:
                search = [['id', '=', int(param['id_ambil'])]]
                data_ambil = models.execute_kw(DB, id_user, password,
                    'ambil.ambil', 'search_read',
                    [search],
                    {'order' : 'name desc', 'offset': 0, 'limit': 1}
                )
                if data_ambil:
                    vals = {
                        'state': 'sukses',
                        'dari': 'dari'
                    }
                    datasetor = models.execute_kw(DB, id_user, password, 'setor.setor', 'write', [[int(data_ambil[0]['no_penyetoran'][0])], vals])
        return json.dumps(datasetor)
        
@app.route("/ambil_delete_api", methods=['POST'])
def ambil_delete_api():
    if request.method == 'POST':
        models = xmlrpc.client.ServerProxy('{}/xmlrpc/2/object'.format(URL))
        request_data = request.get_data ()
        param = json.loads(request_data)

        id_ambil = int(param['id_ambil'])
        id_user = int(param['id_user'])
        password = param['pasword']

        delete_data = models.execute_kw(DB, id_user, password, 'ambil.ambil', 'unlink', [[int(id_ambil)]])
        return json.dumps(delete_data)

@app.route('/ambil_input_api', methods=['POST', 'GET'])
def ambil_input_api():
    if request.method == 'POST':
        models = xmlrpc.client.ServerProxy('{}/xmlrpc/2/object'.format(URL))
        request_data = request.get_data ()
        param = json.loads(request_data)

        id_setor = int(param['id_setor'])
        id_user = int(param['id_user'])
        partner_pengambil = int(param['partner_pengambil'])
        password = param['pasword']

        sampah_harga = 0
        tanggal = datetime.today().strftime("%Y-%m-%d")

        setor = models.execute_kw(DB, id_user, password,
            'setor.setor', 'search_read',
            [[['id', '=', id_setor]]],
            {'fields' : ['sampah_harga']}
        )
        if len(setor) >= 1:
            sampah_harga = setor[0]['sampah_harga']

        vals = {
            "name": "New",
            "tanggal": tanggal,
            "partner_pengambil": partner_pengambil,
            "no_penyetoran": int(id_setor),
            "sampah_harga": float(sampah_harga),
        }

        add_ambil = models.execute_kw(DB, id_user, password, 'ambil.ambil', 'create', [vals])
        result = {'id_ambil': add_ambil}
        return json.dumps(result)

@app.route('/laporan_api', methods=['GET'])
def laporan_api():
    if 'id_user' in session:
        if session['id_user']:
            models = xmlrpc.client.ServerProxy('{}/xmlrpc/2/object'.format(URL))
            setor = models.execute_kw(DB, 2, 'adminwaste1234',
                'setor.setor', 
                'read_group',
                [[['tanggal', '!=', False], ['state', '!=', 'cancel']]],
                {
                    'fields': ['tanggal', 'sampah_nama', 'jenis_sampah', 'sampah_berat', 'sampah_harga'], 
                    'groupby' : ['tanggal']
                },
            )
            datalap = []
            datalap2 = []
            if setor:
                for line in setor:
                    tgl1 = line['__range']['tanggal']['from']
                    tgl2 = line['__range']['tanggal']['to']
                    datalap.append({
                        "tanggal": line['tanggal'],
                        "nama": "",
                        "jenis": "",
                        "tgl1": tgl1,
                        "tgl2": tgl2,
                        "berat": line['sampah_berat'],
                    })
                newlist = sorted(datalap, key=lambda d: d['tgl1'], reverse=True) 
                for line2 in newlist:
                    tgl1 = line2['tgl1']
                    tgl2 = line2['tgl2']
                    setorline = models.execute_kw(DB, 2, 'adminwaste1234',
                        'setor.setor', 'search_read',
                        [[['tanggal', '>=', tgl1], ['tanggal', '<', tgl2]]],
                        {
                            'fields': ['tanggal', 'sampah_nama', 'jenis_sampah', 'sampah_berat', 'sampah_harga', 'sampah_satuan'],
                            'order' : 'tanggal desc'
                        }
                    )
                    sat = ""
                    for zz in setorline:
                        sat = zz['sampah_satuan']
                        datalap2.append({
                            "tanggal": zz['tanggal'],
                            "nama": zz['sampah_nama'],
                            "jenis": zz['jenis_sampah'],
                            "tgl1": "",
                            "tgl2": "",
                            "berat": int(zz['sampah_berat']),
                            "satuan": sat
                        })
                    datalap2.append({
                        "tanggal": "",
                        "nama": "",
                        "jenis": "Total " + line2['tanggal'],
                        "tgl1": "",
                        "tgl2": "",
                        "berat": int(line2['berat']),
                        "satuan": sat
                    })

                totalall = 0
                for zzz in newlist:
                    totalall += int(zzz['berat'])

                datalap2.append({
                    "tanggal": "",
                    "nama": "",
                    "jenis": "Total ALL",
                    "tgl1": "",
                    "tgl2": "",
                    "berat": totalall,
                    "satuan": sat
                })
            html =  render_template(
                'backend/laporan_api.html', 
                title=TITLE,
                datalap2=datalap2
            )

            filepdf = render_pdf(HTML(string=html), download_filename='laporan_sampah.pdf')

            byte = HTML(string=html).write_pdf()
            encoded = base64.b64encode(byte)
            encoded = encoded.decode('utf-8')
            return filepdf

            # return render_template(
            #     'backend/laporan_api.html', 
            #     title=TITLE,
            #     datalap2=datalap2
            # )

        else:
            return redirect(url_for('logout'))
    else:
        return redirect(url_for('logout'))


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8082, debug=True)


# API DOCT
# http://localhost:8082/login_api
# method : POST
# payload : {"username": "admin@admin.co", "pasword": "admin"}

# http://localhost:8082/partner_pengambil_api
# method : POST
# payload : { "id_user": 7, "pasword": "setor2"}

# http://localhost:8082/partner_sampah_api
# method : POST
# payload : { "id_user": 7, "pasword": "setor2"}

# http://localhost:8082/qrcode_setor/id_setor
# method : GET

# http://localhost:8082/setor_api
# method : POST
# payload : {"offset": 0, "limit": 1, "id_user": 2, "pasword": "admin", "id_setor": "9"}
# id_setor = jika semua data isi '%' jika satu data isi id_penyetoran (offset harus 0)

# http://localhost:8082/setor_input_api
# method : POST
# payload : {
#     "id_user": 7,
#     "pasword":"setor2",
#     "partner_pengambil": 9,
#     "mtd_pengambilan": "diambil", #pilihan (diambil / diantar)
#     "partner_sampah": 11,
#     "jenis_sampah": "organik", #pilihan (organik / anorganik)
#     "sampah_harga": 150000,
#     "sampah_nama": "nama sampah",
#     "sampah_berat": 20,
#     "sampah_satuan": "Kg",
#     "tanggal": "2023-03-30"
# }
# return : integer id penyetoran

# http://localhost:8082/setor_set_state_api
# method : POST
# payload : { 
#     "id_user": 7, 
#     "pasword": "setor2", 
#     "id_setor": 11, 
#     "state": "cancel" #pilihan (draft, ready, proses, sukses, cancel)
# }
# return : true/false

# http://localhost:8082/setor_set_state_api
# method : POST
# payload : { 
#     "id_user": 7, 
#     "pasword": "setor2", 
#     "id_setor": 11, 
#     "state": "cancel" #pilihan (draft, ready, proses, sukses, cancel)
# }
# return : true/false

# http://localhost:8082/setor_delete_api
# method : POST
# payload : { 
#     "id_user": 7, 
#     "pasword": "setor2", 
#     "id_setor": 11
# }
# return : true/false

# http://localhost:8082/ambil_api
# method : POST
# payload : {"offset": 0, "limit": 1, "id_user": 2, "pasword": "admin", "id_ambil": "9"}
# id_ambil = jika semua data isi '%' jika satu data isi id_penyetoran (offset harus 0)

# http://localhost:8082/ambil_set_state_api
# method : POST
# payload : { 
#     "id_user": 8, 
#     "pasword": "ambil1", 
#     "id_ambil": 12, 
#     "state": "cancel" #pilihan (draft, ready, proses, sukses, cancel)
# }
# return : true/false

# http://localhost:8082/ambil_delete_api
# method : POST
# payload : { 
#     "id_user": 8, 
#     "pasword": "ambil1", 
#     "id_ambil": 12, 
# }
# return : true/false

# http://localhost:8082/ambil_input_api
# method : POST
# payload : { 
#     "id_user": 8, 
#     "pasword": "ambil1", 
#     "id_setor": 9
# }
# return : integer id penyetoran
# flownya scan dulu qrcode akan dapat value id_setor, lalu masukkan value tersebut ke payloadnya

# ----------------------------------------------------------------------