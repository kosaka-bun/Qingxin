package de.honoka.android.xposed.qingxin;

import org.junit.Test;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

	@Test
	public void test1() {
		Thread thread = new Thread(() -> {
			for(; ; ) {
				try {
					System.out.println("子线程运行...");
					Thread.sleep(1000);
				} catch(InterruptedException e) {
					//ignore
				}
			}
		});
		thread.start();
		try {
			System.out.println("开始等待");
			thread.join(5000);
			System.out.println("等待完成");
			thread.stop();
			for(; ; ) {
				try {
					Thread.sleep(1000);
				} catch(InterruptedException e) {
					//ignore
				}
			}
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}

	//@Test
	//public void addition_isCorrect() {
	//	assertEquals(4, 2 + 2);
	//}
}