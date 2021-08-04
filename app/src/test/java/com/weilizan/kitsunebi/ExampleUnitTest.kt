package com.weilizan.kitsunebi

import org.junit.Test

import org.junit.Assert.*
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        // 按指定模式在字符串查找
        val line = """Unterminated array at character 41 of {
      "outbounds": [
        
          "protocol": "vmess",
          """
        val pattern = """at character [1-9]\d*"""
        val r: Pattern = Pattern.compile(pattern)
        val m: Matcher = r.matcher(line)
        if (m.find()) {
            val text=m.group()
            val num=text.replace("at character ","").toInt()
            println(num)
        } else {

        }
    }
}
