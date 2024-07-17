#Melisa Karadağ
#Oğulcan Tunç Tayan
#Sude Önder

import numpy as np
import librosa
import pickle
from scipy.ndimage import maximum_filter, label, find_objects
from collections import defaultdict


def compute_stft(audio, sr=22050, n_fft=2048, hop_length=512):
    stft = librosa.stft(audio, n_fft=n_fft, hop_length=hop_length)
    magnitude = np.abs(stft)
    return magnitude


def generate_fingerprint(magnitude, threshold=0.1, fan_value=5):
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


# Load existing fingerprints if the file exists
def load_data():
    try:
        with open('fingerprints.pkl', 'rb') as f:
            return pickle.load(f)
    except FileNotFoundError:
        return []


def save_data(fingerprints):
    with open('fingerprints.pkl', 'wb') as f:
        pickle.dump(fingerprints, f)


fingerprints = load_data()


def add_song(fingerprints):
    song_path = input("Enter the path to the song file: ").strip()
    if not song_path:
        print("File path cannot be empty.")
        return

    song_name = input("Enter the song name: ").strip()
    if not song_name:
        print("Song name cannot be empty.")
        return

    artist_name = input("Enter the artist name: ").strip()
    if not artist_name:
        print("Artist name cannot be empty.")
        return

    song_type = input("Enter the song type: ").strip()
    if not song_type:
        print("Song type cannot be empty.")
        return

    try:
        audio, sr = librosa.load(song_path, sr=22050)
    except FileNotFoundError:
        print("File not found.")
        return

    magnitude = compute_stft(audio, sr, n_fft=4096, hop_length=1024)
    fingerprint = generate_fingerprint(magnitude, threshold=0.2, fan_value=10)

    fingerprints.append({
        'song_name': song_name,
        'artist_name': artist_name,
        'song_type': song_type,
        'fingerprint': fingerprint
        #bWVsaXNhb2d1bGNhbnN1ZGU=
    })

    save_data(fingerprints)
    print(f"Fingerprint for '{song_name}' by {artist_name} ({song_type}) saved to database.")


def delete_song(fingerprints):
    song_name = input("Enter the name of the song to delete: ").strip().lower()
    found = False
    for i, song in enumerate(fingerprints):
        if song['song_name'].lower() == song_name:
            del fingerprints[i]
            save_data(fingerprints)
            print(f"'{song['song_name']}' successfully deleted.")
            found = True
            break
    if not found:
        print("Song not found.")



def main():
    while True: 
        choice = input("What would you like to do? (Add/Delete/Fetch/Quit): ").lower()
        if choice == 'add':
            add_song(fingerprints)
        elif choice == 'delete':
            delete_song(fingerprints)
        elif choice == 'fetch':
            if not fingerprints:
                print("No songs in the database.")
            else:
                for song in fingerprints:
                    print(f"{song['song_name']} by {song['artist_name']} ({song['song_type']})")
        elif choice == 'quit':
            print("Exiting and saving data...")
            save_data(fingerprints)
            break
        else:
            print("Invalid choice. Please enter 'Add', 'Delete', 'Fetch', or 'Quit'.")


if __name__ == "__main__":
    main()
