/*
By: Kenan Krijestorac
Professor Hauschild
CS 3780
Project 1 - Task 3
*/

#include <iostream>
#include <bits/stdc++.h>
using namespace std;

int addition(int, int);
int multiplication(int, int);
int division(int, int);

int main() {

	int input1, input2, add, multiply, divide;

	cout << "Enter first integer: " << endl;
	cin >> input1;

	//Tests to see if the first integer is larger or less than the bounds of int
	if ((input1 < INT_MIN) || (input1 > INT_MAX)) {
		cout << "ERROR: OVERFLOW" << endl;
		return -1;
	}

	cout << "Enter second integer: " << endl;
	cin >> input2;

	//Tests to see if the second integer is larger or less than the bounds of int
	if ((input1 < INT_MIN) || (input1 > INT_MAX)) {
		cout << "ERROR: INPUT INTEGER OVERFLOW" << endl;
		return -1;
	}

	cout << "~~~~~~~~~~~~Calculating~~~~~~~~~~~~" << endl;	

	//Checks to see if an exception is generated when addition is performed
	try {
		add = addition(input1, input2);
		cout << "Addition Calculations: " << add << endl;
	}
	catch (runtime_error e1) {
		cout << "ADDITION OVERFLOW" << endl;
	}

	//Checks to see if an exception is generated when multiplication is performed
	try {
		multiply = multiplication(input1, input2);
		cout << "Multiplication Calculation: " << multiply << endl;
	}
	catch (runtime_error e2) {
		cout << "MULTIPLICATION OVERFLOW" << endl;
	}

	//Checks to see if an exception is generated when division is performed
	try {
		divide = division(input1, input2);
		cout << "Division Calculation: " << divide << endl;
	}
	catch (runtime_error e3) {
		cout << "DIVISION OVERFLOW" << endl;
	}

	cout << "~~~~~~~~~~~~Calculations Completed~~~~~~~~~~~~" << endl;

	return 0;
}

int addition(int x, int y) {
	bool flag = true;
	flag = ((x > 0) && (y > (INT_MAX - x))) || (x < 0) && (y < (INT_MIN - x));

	if(flag == true){
		throw runtime_error("OVERFLOW WHEN PERFORMING ADDITION");
	}
	else{
		return x + y;
	}
}

int multiplication(int x, int y) {
	bool flag = true;
	flag = (y > (INT_MAX / x)) || (y < (INT_MIN / x));

	if (flag == true){
		throw runtime_error("OVERFLOW WHEN PERFORMING MULTIPLICATION");
	}
	else{
		return x * y;
	}
}

int division(int x, int y) {
	bool flag = true;
	flag = (y == 0) || ((x == INT_MIN) && y == -1);

	if (flag == true) {
		throw runtime_error("OVERFLOW WHEN PERFORMING DIVISION");
	}
	else {
		return x / y;
	}
}
