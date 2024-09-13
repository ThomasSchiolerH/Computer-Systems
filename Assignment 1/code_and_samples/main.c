//To compile (linux/mac): gcc cbmp.c main.c -o main.out -std=c99
//To run (linux/mac): ./main.out example.bmp example_inv.bmp

//To compile (win): gcc cbmp.c main.c -o main.exe -std=c99
//To run (win): ./main.exe example.bmp final_image.bmp

#include <stdlib.h>
#include <stdio.h>
#include "cbmp.h"
#include <time.h>
#define detectSize 8

//Declaring the array to store the image (unsigned char = unsigned 8 bit)
unsigned char input_image[BMP_WIDTH][BMP_HEIGTH][BMP_CHANNELS];
unsigned char inverted_image[BMP_WIDTH][BMP_HEIGTH][BMP_CHANNELS];

unsigned char bitMapImage[BMP_WIDTH][BMP_HEIGTH][BMP_CHANNELS];
unsigned char tempErodeImage[BMP_WIDTH][BMP_HEIGTH][BMP_CHANNELS];
int crossMatrix[4][2] = {{-1,0},{1,0},{0,-1},{0,1}};
int cellCount = 0;
int coordinates [1000][2];
clock_t start, end;

//Function to invert pixels of an image (negative)
void invert(unsigned char input_image[BMP_WIDTH][BMP_HEIGTH][BMP_CHANNELS], unsigned char output_image[BMP_WIDTH][BMP_HEIGTH][BMP_CHANNELS]){
  for (int x = 0; x < BMP_WIDTH; x++) {
    
    for (int y = 0; y < BMP_HEIGTH; y++) {

      for (int c = 0; c < BMP_CHANNELS; c++) {
      output_image[x][y][c] = 255 - input_image[x][y][c];
      }
    }
  }
}

//convert bitmap to binary map
void toBinary(unsigned char input_image[BMP_WIDTH][BMP_HEIGTH][BMP_CHANNELS], unsigned char output_image[BMP_WIDTH][BMP_HEIGTH][BMP_CHANNELS]){
  for (int x = 0; x < BMP_WIDTH; x++) {

    for (int y = 0; y < BMP_HEIGTH; y++) {

      int res = 0;
      for (int c = 0; c < BMP_CHANNELS; c++) {
      res += input_image[x][y][c];
      }
      
      for (int i = 0; i < BMP_CHANNELS; i++){
        output_image[x][y][i] = res / BMP_CHANNELS > 130 ? 0: 255;
      }
    }
  }
}

//checks if whitespots exists
int whiteSpotsExists(unsigned char input_image[BMP_WIDTH][BMP_HEIGTH][BMP_CHANNELS]){
  for (int x = 0; x < BMP_WIDTH; x++) {
      for (int y = 0; y < BMP_HEIGTH; y++) {
        int res = 0;
        for (int c = 0; c < BMP_CHANNELS; c++) {
        res += input_image[x][y][c];
        }
        
        if (res / BMP_CHANNELS > 130){
          return 1;
        } else {
          continue;
        }
      }
    }
  return 0;
}

//checks for coordinates inside matrix
int insideMatrix(int x, int y){
  if (x >= 0 && x < BMP_WIDTH && y >= 0 && y < BMP_HEIGTH){
    return 1;
  }
  return 0;
}

//erodes image
void erode(unsigned char orginalImage[BMP_WIDTH][BMP_HEIGTH][BMP_CHANNELS], unsigned char tempImage[BMP_WIDTH][BMP_HEIGTH][BMP_CHANNELS]){
  for (int x = 0; x < BMP_WIDTH; x++) {
    for (int y = 0; y < BMP_HEIGTH; y++) {
      
      for (int i = 0; i < 4; i++){
        if (insideMatrix(x + crossMatrix[i][0], y + crossMatrix[i][1]) && 
                      orginalImage[x + crossMatrix[i][0]][y + crossMatrix[i][1]][2] == 0){

          for (int i = 0; i < BMP_CHANNELS; i++){
            tempImage[x][y][i] = 0;
          }
          break;
        } 
        else {
          continue;
        }
      }
    }
  }
}

//goes through every pixel and checks if a white dot fits insideMatrix
int frameScan(unsigned char bitMapImage[BMP_WIDTH][BMP_HEIGTH][BMP_CHANNELS], int x, int y)   {
    for (int i = - detectSize; i <= detectSize; i++) {
        if(i == - detectSize || i == detectSize) {
            for (int j = 1 - detectSize; j <= detectSize; j++) {
                if(insideMatrix(x + i, y + j) && bitMapImage[x + i][y + j][2] != 0) {
                    return 0;
                }
            }
        } else if(bitMapImage[x + i][y - detectSize + 1][2] != 0 || bitMapImage[x + i][y + detectSize][2] != 0) {
            return 0;
        } 
    }
    return 1;
}

// sets all pixels to black
void toBlack(unsigned char bitMapImage[BMP_WIDTH][BMP_HEIGTH][BMP_CHANNELS], int x, int y){
    for (int i = - detectSize; i <= detectSize - 1; i++) {
        for (int j = - detectSize; j <= detectSize - 1; j++){
          if (insideMatrix(x + i , y + j)){
            bitMapImage[x + i][y + j][2] = 0;
          }
        }
    }
}

// counts cells and sets all pixels in the matrix to black after a cell is counted
void findCells(unsigned char bitMapImage[BMP_WIDTH][BMP_HEIGTH][BMP_CHANNELS]) {
    for (int x = 0; x < BMP_WIDTH; x++){    
        for (int y = 0; y < BMP_WIDTH; y++){
            if(bitMapImage[x][y][2] != 0) {
                if(frameScan(bitMapImage, x, y) == 1) {
                    toBlack(bitMapImage, x, y);
                    coordinates[cellCount][0] = x;
                    coordinates[cellCount][1] = y;
                    cellCount++;
                }
            }
        }
    }
}

// Marking the cells with a red cross
void markWithCross(unsigned char inputImage[BMP_WIDTH][BMP_HEIGTH][BMP_CHANNELS]){
  int crossLength = 10;

  for (int i = 0; i < cellCount; i++){
    if(insideMatrix(coordinates[i][0],coordinates[i][1])){
      for (int k = -crossLength; k < crossLength + 1; k++){
      for (int c = 0; c < BMP_CHANNELS; c++){
        if(c == 0){
          inputImage[coordinates[i][0] + k][coordinates[i][1]][c] = 255;
          inputImage[coordinates[i][0]][coordinates[i][1] + k][c] = 255;
        } else {
          inputImage[coordinates[i][0] + k][coordinates[i][1]][c] = 0;
          inputImage[coordinates[i][0]][coordinates[i][1] + k][c] = 0;
        }
       }
      }
    }
  }
}

// Copies all pixels from image to another
void copyImage(unsigned char input_image[BMP_WIDTH][BMP_HEIGTH][BMP_CHANNELS], unsigned char output_image[BMP_WIDTH][BMP_HEIGTH][BMP_CHANNELS]){
  for (int x = 0; x < BMP_WIDTH; x++) {
    for (int y = 0; y < BMP_HEIGTH; y++) {
      for (int c = 0; c < BMP_CHANNELS; c++){
        output_image[x][y][c] = input_image[x][y][c];
      }
    }
  }
}

// Main function
int main(int argc, char** argv)
{
  // Checking that 2 arguments are passed
  if (argc != 3)
  {
      fprintf(stderr, "Usage: %s <output file path> <output file path>\n", argv[0]);
      exit(1);
  }

  printf("02132 - A1\n");

  // Starting of timer
  double timeUsed;
  start = clock();


  // Load image from file
  read_bitmap(argv[1], input_image);

  // Run inversion
  invert(input_image,inverted_image);

  // Converts image to binary image
  toBinary(inverted_image, bitMapImage); 

  copyImage(bitMapImage, tempErodeImage);

  // Initial erode
  for (int i = 0; i  < 8; i++){
    copyImage(tempErodeImage, bitMapImage);
    findCells(bitMapImage);
    erode(bitMapImage, tempErodeImage);
  }
  

  // Erosion
  while(whiteSpotsExists(bitMapImage)){
    copyImage(tempErodeImage, bitMapImage);
    findCells(bitMapImage);
    erode(bitMapImage, tempErodeImage);
  }

  printf("%d \n",cellCount);
    
  markWithCross(input_image);

  // Save image to file
  write_bitmap(input_image, argv[2]);
  

  printf("Done!\n");

  // Ending of timer
  end = clock();
  timeUsed = end - start;
  printf("Execution time: %.2f ms\n", timeUsed * 1000.0 / CLOCKS_PER_SEC);

  return 0;
}
