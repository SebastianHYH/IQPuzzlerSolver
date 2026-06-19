# Tugas Kecil 1 Strategi Algoritma

Program ini dinamakan IQ Puzzler Solver, sebuah program yang dapat menyelesaikan tantangan-tantangan dari permainan IQ Puzzler Pro. Solusi yang diberikan program dapat disimpan sebagai file teks ataupun file gambar

## Requirements
- Latest version of Java and its libraries

## How to Run
1. Clone repository menggunakan
   ```sh
   git clone https://github.com/SebastianHYH/Tucil1_13523070.git
2. Buka directory program di
   ```sh
   ..\src\
3. Compile program dengan
   ```sh
   javac IQPuzzlerSolver.java SaveAsImage.java
4. Jalankan program dengan
   ```sh
   java IQPuzzlerSolver
   ```
   Secara default program menggunakan algoritma brute force. Untuk menjalankan dengan algoritma backtracking yang lebih cepat (heuristik), tambahkan argumen `--fast`:
   ```sh
   java IQPuzzlerSolver --fast
   ```
5. Ketika program sudah dijalankan, anda akan diminta untuk memasukkan path test case

## Test case format (.txt)
```sh
N M P
S
puzzle_1_shape
puzzle_2_shape
...
puzzle_P_shape
```
- N dan M membentuk papan berukuran NxM
- P menyatakan jumlah piece puzzle
- S untuk mengidentifikasi konfigurasi papan (DEFAULT/CUSTOM)
- puzzle_shape merepresentasikan bentuk piece puzzle menggunakan huruf kapital A-Z
- Contoh-contoh test case beserta solusinya dapat dilihat di dalam folder ..\test\

## Identitas Pembuat
Sebastian Hung Yansen - 13523070
