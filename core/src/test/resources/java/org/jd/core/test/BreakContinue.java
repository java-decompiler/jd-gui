/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.test;

public class BreakContinue {

	public void doWhileContinue(int i) {
		System.out.println("start");

		do {
			System.out.println("a");

			if (i == 1) {
				continue;
			}
			if (i == 2) {
				continue;
			}

			System.out.println("b");
		} while (i > 5 && i < 10);

		System.out.println("end");
	}

	public void tripleWhile1(int i) {
		System.out.println("start");

		label: while (i > 1) {
			while (i > 2) {
				while (i > 3) {
					System.out.println("a");

					if (i == 4) {
						continue label;
					}
					if (i == 5) {
						break label;
					}

					System.out.println("b");
				}
			}
		}

		System.out.println("end");
	}

	public void tripleWhile2(int i) {
		System.out.println("start");

		while (i > 1) {
			label: while (i > 2) {
				while (i > 3) {
					System.out.println("a");

					if (i == 4) {
						continue label;
					}
					if (i == 5) {
						break label;
					}

					System.out.println("b");
				}
			}
		}

		System.out.println("end");
	}

	public void tripleDoWhile1(int i) {
		System.out.println("start");

		label: do {
			do {
				do {
					System.out.println("a");

					if (i == 1) {
						continue label;
					}
					if (i == 2) {
						break label;
					}

					System.out.println("b");
				} while (i > 3);
			} while (i > 4);
		} while (i > 5);

		System.out.println("end");
	}

	public void tripleDoWhile2(int i) {
		System.out.println("start");

		label0: do {
			label: do {
				do {
					System.out.println("a");

					if (i == 1) {
						continue label;
					}
					if (i == 2) {
						break label;
					}

					System.out.println("b");
				} while (i > 3);
			} while (i > 4);
		} while (i > 5);

		System.out.println("end");
	}

	public int doWhileWhileIf(int i0, int i1) {
		System.out.println("start");

	    label_0: do {
			System.out.println("a");

			label_1: for (int i=i0; i<i1; i++) {
				System.out.println("b");

				while (i0 < 10) {
					if (i0 / i1 != 2) {
						i0 = i0 + 1;
					} else if (i0 % i1 == 123) {
						continue label_0;
					} else if (i0 % i1 == 456) {
						break label_0;
					} else {
						break label_1;
					}
				}

				System.out.println("c");
			}

	        i1 = i1 + -1;
	    } while (i1 > 0);

		System.out.println("end");

	    return i0;
	}

	public int doWhileWhileTryBreak(int i0, int i1) {
		System.out.println("start");

	    label_0: do {
	        while (i0 < 10) {
	            try {
	                if (i0 / i1 != 2) {
	                    i0 = i0 + 1;
	                } else {
	                    break label_0;
	                }
	            } catch (ArithmeticException e) {
	                System.out.println("div by 0");
	            }
	        }

	        i1 = i1 + -1;
	    } while (i1 > 0);

		System.out.println("end");

	    return i0;
	}
}
