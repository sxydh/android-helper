package cn.net.bhe.androidhelper.utils

import cn.net.bhe.mutil.FlUtils
import cn.net.bhe.mutil.StrUtils
import cn.net.bhe.mutil.UrlUtils
import fi.iki.elonen.NanoHTTPD
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.Base64

object FileServerUtils {

    fun build(port: Int, root: String): FileServer {
        return build("localhost", port, root)
    }

    fun build(host: String, port: Int, root: String): FileServer {
        return build(host, port, root, StrUtils.EMPTY, StrUtils.EMPTY)
    }

    fun build(host: String, port: Int, root: String, username: String, password: String): FileServer {
        return FileServer(host, port, root, username, password)
    }

}

open class FileServer(host: String, port: Int, private val root: String, private val username: String, private val password: String) : NanoHTTPD(host, port) {

    override fun serve(session: IHTTPSession): Response {
        if (!authenticateUser(session)) {
            return process401(session)
        }

        var path = session.uri
        path = UrlUtils.decode(path)
        path = StrUtils.trim(path, "/", true, false)
        path = FlUtils.combine(root, path)

        val file = File(path)
        if (!file.exists()) {
            return process404(session)
        }

        if (file.isDirectory) {
            return processDirectory(session, file)
        }

        return processFile(session, file)
    }

    private fun authenticateUser(session: IHTTPSession): Boolean {
        if (username.isEmpty() && password.isEmpty()) {
            return true
        }

        val authorization = session.headers["authorization"] ?: return false
        if (authorization.isEmpty()) {
            return false
        }

        val prefix = "Basic "
        if (!authorization.startsWith(prefix)) {
            return false
        }

        val encodedCredentials = authorization.substring(prefix.length).trim()
        val credentials = String(Base64.getDecoder().decode(encodedCredentials), StandardCharsets.UTF_8)
        val parts = credentials.split(":")

        return parts.size == 2 && parts[0] == username && parts[1] == password
    }

    open fun process401(session: IHTTPSession): Response {
        val response = newFixedLengthResponse(Response.Status.UNAUTHORIZED, "text/plain", StrUtils.EMPTY)
        response.addHeader("WWW-Authenticate", "Basic realm=\"MyRealm\"")
        return response
    }

    open fun process404(session: IHTTPSession): Response {
        return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", StrUtils.EMPTY)
    }

    open fun processDirectory(session: IHTTPSession, directory: File): Response {
        val indexFile = File(directory, "index.html")
        if (indexFile.exists() && indexFile.isFile) {
            return processFile(session, indexFile)
        }

        val subDirectories = directory.listFiles { file -> file.isDirectory }?.sortedBy { it.name } ?: emptyList()
        val subFiles = directory.listFiles { file -> file.isFile }?.sortedBy { it.name } ?: emptyList()

        val htmlBuilder = StringBuilder()
        htmlBuilder.append("<html><head><meta charset=\"UTF-8\"></head><body><ul>")
        for (subDir in subDirectories) {
            htmlBuilder.append("<li><a href=\"${UrlUtils.encode(subDir.name)}/\">${subDir.name}/</a></li>")
        }
        for (subFile in subFiles) {
            htmlBuilder.append("<li><a href=\"${UrlUtils.encode(subFile.name)}\">${subFile.name}</a></li>")
        }
        htmlBuilder.append("</ul></body></html>")

        return newFixedLengthResponse(Response.Status.OK, "text/html; charset=UTF-8", htmlBuilder.toString())
    }

    open fun processFile(session: IHTTPSession, file: File): Response {
        return newChunkedResponse(
            Response.Status.OK,
            cn.net.bhe.mutil.FileServerUtils.getContentType(file.name),
            file.inputStream()
        )
    }

}