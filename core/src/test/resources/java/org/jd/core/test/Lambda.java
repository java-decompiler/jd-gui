/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.test;

import java.lang.invoke.MethodType;
import java.util.List;
import java.util.function.*;
import java.util.stream.Collectors;

public class Lambda {
	protected int index;

	public void printListItems1(List<String> list) {
		list.forEach(System.out::println);
	}
	
	public void printListItems2(List<String> list) {
		list.stream().filter(s -> s != null).forEach(s -> System.out.println(s));
	}
	
	public void printListItems3(List<String> list, int length) {
		Predicate<String> filter = s -> s.length() == length;
		Consumer<String> println = s -> System.out.println(s);
		
		list.stream().filter(filter).forEach(println);
	}
	
	public void printMapItems(List<Lambda> list) {
		list.stream()
			.collect(Collectors.toMap(lambda -> lambda.index, Function.identity()))
			.forEach(
				(key, value) -> 
				System.out.println(key + " --> " + value));
	}
	
	public void startThread1() {
		Thread thread = new Thread(() -> {
			System.out.println("hello");
			System.out.println("world");
		});
		
		thread.start();
	}

	public void startThread2(String message, int count) {
		Thread thread = new Thread(() -> {
			for (int i=0; i<count; i++) {
				System.out.println(message);
			}
		});
		
		thread.start();
	}
	
	public void references(String s) {
		Consumer<String> staticMethodReference = String::valueOf;
		BiFunction<String, String, Integer> methodReference = String::compareTo;
		Supplier<String> instanceMethodReference = s::toString;
		Supplier<String> constructorReference = String::new;
	}
	
	public void methodTypes() {
		MethodType mtToString = MethodType.methodType(String.class);
		MethodType mtSetter = MethodType.methodType(void.class, Object.class);
		MethodType mtStringComparator = MethodType.methodType(int[].class, String.class, String.class);
	}
}
