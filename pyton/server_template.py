import socket
import threading
import time
import websocket

import websocket
import threading

import socket
import dns.resolver

PORT = 5050
HEADER = 64
SERVER = socket.gethostbyname(socket.gethostname())
# result = dns.resolver.resolve('minik.pythonanywhere.com', 'A')
# SERVER = result.nameserver
# PORT = result.port
ADDR = (SERVER, PORT)
FORMAT = 'utf-8'
DISCONNECT_MSG = "DISCOqNECT!"



def receive(conn):
    msg_len = conn.recv(HEADER).decode(FORMAT)
    if msg_len:
        msg_len = int(float(msg_len))
        msg = conn.recv(msg_len).decode(FORMAT)
        return msg

def send(conn,msg):
    message = msg.encode(FORMAT)
    msg_len = len(message)
    send_length = str(msg_len).encode(FORMAT)
    send_length += b' ' * (HEADER - len(send_length))
    conn.send(send_length)
    conn.send(message)
    
def read_right_left(x, last_x, sec_last_x, spikes):
    if max([sec_last_x, last_x, x]) == last_x or min([sec_last_x, last_x, x]) == last_x:
        spikes.append(last_x)
        if len(spikes) == 3:
            if last_x > 0:
                    # prawo
                return "RIGHT"
            else:
                return "LEFT"
    return spikes

def read_up_down(x, last_x, sec_last_x, spikes):
    if max([sec_last_x, last_x, x]) == last_x or min([sec_last_x, last_x, x]) == last_x:
        if abs(last_x) > 0.5:
            spikes.append(last_x)
        if len(spikes) == 3:
            if last_x > 0:
                return "UP"
            else:
                return "DOWN"
    return spikes


def handle_client(conn : socket.socket, addr, ws : websocket.WebSocketApp):
    print(f"[NEW CONNECTION] {addr} connected")
    connected = True
    last_x = 0
    sec_last_x = 0
    spikes = []
    last_z = 0
    sec_last_z = 0
    spikesz = []
    results = []
    with open("data_out.txt", "w") as myfile:
        while connected:
            msg = receive(conn)
            if msg == DISCONNECT_MSG:
                connected = False
                continue
            send(conn, f"{msg}")
            
            values = msg.strip().split(',')
            x_values = float(values[0])
            y_values = float(values[1])
            z_values = float(values[2])
            
            if abs(last_x) > 1 or len(spikes) != 0 and len(spikesz) == 0:
                spikes = read_right_left(x_values, last_x, sec_last_x, spikes)
                if type(spikes) == str:
                    ws.send(spikes)
                    spikes = []
                # elif len(spikes) == 3:
                #     spikes = [spikes[2]]
            
            elif abs(last_z) > 1 or len(spikesz) != 0:
                spikesz = read_up_down(z_values, last_z, sec_last_z, spikesz)
                if type(spikesz) == str:
                    ws.send(spikesz)
                    spikesz = []
                # elif len(spikes) == 3:
                #     spikes = [spikes[2]]
            sec_last_z = last_z
            last_z = z_values
            sec_last_x = last_x
            last_x = x_values
            
            
            # myfile.write(msg+"\n")
            
    conn.close()
    pass

def start(ws : websocket.WebSocketApp):
    server = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
    server.bind(ADDR)

    server.listen()
    print(f"[LISTENING] on addres {SERVER} at port {PORT}")
    while True:
        conn, addr = server.accept()
        thr = threading.Thread(target = handle_client, args = (conn, addr, ws))
        thr.start()
        print(f"[ACTIVE CONNECTIONS] {threading.active_count() - 1}")
    pass



def on_message(ws, message):
    # print(message)
    pass

def on_error(ws, error):
    print(error)

def on_close(ws):
    print("Closing")

def on_open(ws):
    # Start a separate thread for user input
    threading.Thread(target=get_user_input, args=(ws,)).start()

def get_user_input(ws):
    print("[STARTING] ...")
    start(ws)

websocket.enableTrace(True)
ws = websocket.WebSocketApp("ws://192.168.0.106:2137/Auth",
                            on_message=on_message,
                            on_error=on_error,
                            on_close=on_close)
ws.on_open = on_open
    
ws.run_forever()

 