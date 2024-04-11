import socket
import threading
import time

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

server = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
server.bind(ADDR)

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
        print(spikes)
        if len(spikes) == 3:
            if abs(spikes[1]) > 4.5:
                # print(spikes)
                if last_x > 0:
                    # prawo
                    return 'right'
                else:
                    return 'left'
            else:
                # print(spikes)
                # if x > 0:
                #     # prawo
                #     return 'right', 0, []
                # else:
                #     return 'left', 0, []
                return []
    return spikes


def handle_client(conn : socket.socket, addr):
    print(f"[NEW CONNECTION] {addr} connected")
    connected = True
    last_x = 0
    sec_last_x = 0
    spikes = []
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
            
            if abs(last_x) > 1.5:
                spikes = read_right_left(x_values, last_x, sec_last_x, spikes)
                if type(spikes) == str:
                    print(spikes)
                    spikes = []
            sec_last_x = last_x
            last_x = x_values
    
            myfile.write(msg+"\n")
            
    conn.close()
    pass

def start():
    server.listen()
    print(f"[LISTENING] on addres {SERVER} at port {PORT}")
    while True:
        conn, addr = server.accept()
        thr = threading.Thread(target = handle_client, args = (conn, addr))
        thr.start()
        print(f"[ACTIVE CONNECTIONS] {threading.active_count() - 1}")
    pass

print("[STARTING] ...")
start()
