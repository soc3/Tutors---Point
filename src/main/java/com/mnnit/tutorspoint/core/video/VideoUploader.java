package com.mnnit.tutorspoint.core.video;

import javafx.beans.property.*;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;

import static com.mnnit.tutorspoint.core.Globals.GSON;

public class VideoUploader {

    public static final String CRLF = "\r\n";
    private SimpleObjectProperty<Video> video = new SimpleObjectProperty<>();
    private SimpleStringProperty videoJson = new SimpleStringProperty(GSON.toJson(video));
    private SimpleStringProperty url = new SimpleStringProperty();
    private StringProperty charset = new ReadOnlyStringWrapper("utf-8");
    private SimpleObjectProperty<URLConnection> urlConnection = new SimpleObjectProperty<>();
    private SimpleStringProperty boundary = new SimpleStringProperty(Long.toHexString(System.nanoTime()));
    private SimpleObjectProperty<File> file = new SimpleObjectProperty<>();
    private SimpleObjectProperty<PrintWriter> writer = new SimpleObjectProperty<>();
    private SimpleObjectProperty<OutputStream> output = new SimpleObjectProperty<>();

    {
        video.addListener((observable, oldValue, newValue) -> videoJson.set(GSON.toJson(newValue)));
        output.addListener((observable, oldValue, newValue) -> {
            try {
                writer.set(new PrintWriter(new OutputStreamWriter(newValue, charset.get()), true));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        });
    }

    public VideoUploader() throws Exception {
    }

    public void sendRequest() throws IOException {
        initRequest();
        PrintWriter writer = getWriter();
        writer.append("--" + boundary).append(CRLF);
        writer.append("Content-Disposition: form-data; name=\"metadata\"").append(CRLF);
        writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
        writer.append(CRLF).append(getVideoJson()).append(CRLF).flush();
        // Send binary file.
        writer.append("--" + boundary).append(CRLF);
        writer.append(
                "Content-Disposition: form-data; name=\"binaryFile\"; filename=\"" + file.get().getName() + "\"")
                .append(
                        CRLF);
        writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(file.get().getName())).append(CRLF);
        writer.append("Content-Transfer-Encoding: binary").append(CRLF);
        writer.append(CRLF).flush();
        Files.copy(file.get().toPath(), output.get());
        output.get().flush(); // Important before continuing with writer!
        writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.

        // End of multipart/form-data.
        writer.append("--" + boundary + "--").append(CRLF).flush();
        urlConnection.get().connect();
        urlConnection.get().getInputStream().read(new byte[1]);
    }

    private void initRequest() throws IOException {
        setUrlConnection(new URL(getUrl()).openConnection());
        getUrlConnection().setDoOutput(true);
        getUrlConnection().setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        output.set(getUrlConnection().getOutputStream());
        writer.set(new PrintWriter(new OutputStreamWriter(output.get(), charset.get()), true));
    }

    public PrintWriter getWriter() {
        return writer.get();
    }

    public String getVideoJson() {
        return videoJson.get();
    }

    public void setVideoJson(final String videoJson) {
        this.videoJson.set(videoJson);
    }

    public String getUrl() {
        return url.get();
    }

    public void setUrl(final String url) {
        this.url.set(url);
    }

    public URLConnection getUrlConnection() {
        return urlConnection.get();
    }

    public void setUrlConnection(final URLConnection urlConnection) {
        this.urlConnection.set(urlConnection);
    }

    public void setWriter(final PrintWriter writer) {
        this.writer.set(writer);
    }

    public Video getVideo() {
        return video.get();
    }

    public void setVideo(final Video video) {
        this.video.set(video);
    }

    public SimpleObjectProperty<Video> videoProperty() {
        return video;
    }

    public SimpleStringProperty videoJsonProperty() {
        return videoJson;
    }

    public SimpleStringProperty urlProperty() {
        return url;
    }

    public String getCharset() {
        return charset.get();
    }

    public void setCharset(final String charset) {
        this.charset.set(charset);
    }

    public StringProperty charsetProperty() {
        return charset;
    }

    public SimpleObjectProperty<URLConnection> urlConnectionProperty() {
        return urlConnection;
    }

    public String getBoundary() {
        return boundary.get();
    }

    public void setBoundary(final String boundary) {
        this.boundary.set(boundary);
    }

    public SimpleStringProperty boundaryProperty() {
        return boundary;
    }

    public File getFile() {
        return file.get();
    }

    public void setFile(final File file) {
        this.file.set(file);
    }

    public SimpleObjectProperty<File> fileProperty() {
        return file;
    }

    public SimpleObjectProperty<PrintWriter> writerProperty() {
        return writer;
    }
}
