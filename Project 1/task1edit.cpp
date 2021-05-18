/*
 * By: Kenan Krijestorac
 * Professor Hauschild
 * CS 3780
 * Project 1 - Task 1
 *
 * */


#include <iostream>
using namespace std;

int addFive(int);
int multiplyByTwo(int);

int main() {
	int x = 6;
	int y = 3;

	cout << addFive(x) << endl;;

	return 0;
}

int addFive(int a) {
	int x = a + 5;
	int y = multiplyByTwo(x);

	return y;
}

int multiplyByTwo(int b) {
	int x = b * 2;

	return x;
}
