#Melisa Karadağ
#Oğulcan Tunç Tayan
#Sude Önder


import socket
import pickle
import numpy as np
import librosa
from scipy.ndimage import maximum_filter, label, find_objects
from collections import defaultdict
import threading
import time

def compute_stft(audio, sr=22050, n_fft=2048, hop_length=512, window='hann'):
    stft = librosa.stft(audio, n_fft=n_fft, hop_length=hop_length, window=window)
    magnitude = np.abs(stft)
    return magnitude

def generate_fingerprint(magnitude, threshold_ratio=0.7, fan_value=5):
    mean_magnitude = np.mean(magnitude)
    threshold = mean_magnitude * threshold_ratio
    local_max = maximum_filter(magnitude, size=20) == magnitude
    local_max[magnitude < threshold] = 0
    labeled, num_objects = label(local_max)
    peaks = np.array(find_objects(labeled))

    peak_positions = []
    for i in range(num_objects):
        x, y = peaks[i]
        peak_positions.append((x.start, y.start))

    hash_pairs = []
    for i in range(len(peak_positions)):
        for j in range(1, fan_value):
            if i + j < len(peak_positions):
                freq1, time1 = peak_positions[i]
                freq2, time2 = peak_positions[i + j]
                time_delta = time2 - time1
                hash_pairs.append(((freq1, freq2, time_delta), time1))

    return hash_pairs

def match_fingerprints(fp1, fp2):
    hash_dict = defaultdict(list)
    for h, t in fp1:
        hash_dict[h].append(t)

    matches = []
    for h, t2 in fp2:
        if h in hash_dict:
            for t1 in hash_dict[h]:
                matches.append((t1, t2))

    return matches

def handle_client(conn, addr, fingerprints):
    print('Connected by', addr)
    
    record_seconds = 15
    chunk = 1024
    
    try:
        while True:
            audio_data = b''
            start_time = time.time()
            while time.time() - start_time < record_seconds:
                data = conn.recv(chunk)
                if not data:
                    break
                audio_data += data

            if not audio_data:
                break  # No data received
            
            # Data type fixed and converted to float and normalized
            audio_array = np.frombuffer(audio_data, dtype=np.int16)
            audio_array = audio_array.astype(np.float64) / 32768

            sr = 44100 #bWVsaXNhb2d1bGNhbnN1ZGU=
            magnitude = compute_stft(audio_array, sr, n_fft=4096, hop_length=1024)
            query_fingerprint = generate_fingerprint(magnitude, threshold_ratio=0.7, fan_value=10)

            
            min_matches = 50  # Minimum number of matching hashes to consider a valid match
            
            # Match fingerprints
            matches = []
            for entry in fingerprints:
                file_fingerprint = entry['fingerprint']
                match_count = len(match_fingerprints(query_fingerprint, file_fingerprint))
                if match_count >= min_matches:
                    matches.append((entry, match_count))

            # Find the best match
            if matches:
                best_match = max(matches, key=lambda x: x[1])
                song_info = best_match[0]
                result = f"Best match: '{song_info['song_name']}' by {song_info['artist_name']} ({song_info['song_type']}) with {best_match[1]} matching hashes"
            else:
                result = "No valid match found"

            conn.sendall(result.encode())
    except ConnectionAbortedError:
        print(f"Connection with {addr} aborted by client.")
    except Exception as e:
        print(f"An error occurred with {addr}: {e}")
    finally:
        conn.close()

# Load fingerprints from the file
with open('fingerprints.pkl', 'rb') as f:
    fingerprints = pickle.load(f)

# Set up the server
HOST = 'localhost'
PORT = 5432

with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
    s.bind((HOST, PORT))
    s.listen()
    print('Server is listening...')

    while True:
        conn, addr = s.accept()
        client_thread = threading.Thread(target=handle_client, args=(conn, addr, fingerprints))
        client_thread.start()