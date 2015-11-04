package com.tns;

import java.lang.Exception;
import java.util.Stack;
import java.lang.AutoCloseable;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
import android.os.SystemClock;
import android.util.Log;

public class Profile {

    private static Stack<Frame> frames = new Stack<Frame>();
    private static long startTime = 0;

    private static AutoCloseable startupBlock;

    static void onActivityResumeEnd() {
        try {
            if (Profile.startupBlock != null) {
                Profile.startupBlock.close();
            }
            Profile.startupBlock = null;
        } catch(Exception e) {

        }
        // android.os.Debug.stopMethodTracing();
    }

    static void onApplicationCreateStart() {
        // android.os.Debug.startMethodTracing();
        try {
            Profile.startupBlock = Profile.block("launch", "Launch");

        } catch(Exception e) {

        }
    }

    public static AutoCloseable block(String name) throws Exception {
        //android.os.Trace.beginSection(name);
        //return TraceCloser.instance;
        return new Frame(name);
    }

    public static AutoCloseable block(String domain, String name) throws Exception {
        return new Frame(domain, name);
    }

    public static class TraceCloser implements AutoCloseable {
        static TraceCloser instance = new TraceCloser();

        public void close() throws Exception {
            android.os.Trace.endSection();
        }
    }

    private static class Frame implements AutoCloseable {

        public String domain;
        public String name;
        public double start;
        public double end;
        public int depth;
        public boolean isLeaf;

        public Frame(String domain, String name) throws Exception {
            this.isLeaf = true;
            this.domain = domain;
            this.name = name;
            this.start = getEllapsedTime();
            this.depth = Profile.frames.size();
            if (!Profile.frames.empty()) {
                Profile.frames.peek().makeNode();
            }
            Profile.frames.push(this);
        }

        public Frame(String name) throws Exception {
            this.isLeaf = true;
            this.name = name;
            this.start = getEllapsedTime();
            this.depth = Profile.frames.size();
            if (!Profile.frames.empty()) {
                Profile.frames.peek().makeNode();
            }
            Profile.frames.push(this);
        }

        protected void printStart() {
            // Log.i("TNS/PROFILE", this.depth() + name + " (start: " + this.start + ") {");
            if (domain == null) {
                Log.i("TNS/PROFILE", "<block name=\"" + name + "\"><start>" + this.start + "</start>");
            } else {
                Log.i("TNS/PROFILE", "<block name=\"" + name + "\"><domain>" + this.domain + "</domain><start>" + this.start + "</start>");
            }
        }

        protected void printEnd() {
            // Log.i("TNS/PROFILE", this.depth() + "} " + name + " (end: " + this.end + ", duration: " + (this.end - this.start) + ")");
            Log.i("TNS/PROFILE", "<end>" + this.end + "</end><duration>" + (this.end - this.start) + "</duration></block>");
        }

        protected void printShort() {
            // Log.i("TNS/PROFILE", this.depth() + name + " (start: " + this.start + ", end: " + this.end + ", duration: " + (this.end - this.start) + ");");
            if (domain == null) {
                Log.i("TNS/PROFILE", "<block name=\"" + name + "\"><start>" + this.start + "</start><end>" + this.end + "</end><duration>" + (this.end - this.start) + "</duration></block>");
            } else {
                Log.i("TNS/PROFILE", "<block name=\"" + name + "\"><start>" + this.start + "</start><domain>" + this.domain + "</domain><end>" + this.end + "</end><duration>" + (this.end - this.start) + "</duration></block>");
            }
        }

        protected String depth() {
            String s = "";
            for (int i = 0; i < depth; i++)
                s += "  ";
            return s;
        }

        protected void makeNode() {
            if (this.isLeaf) {
                this.printStart();
                this.isLeaf = false;
            }
        }

        public void close() throws Exception {
            if (Profile.frames.peek() == this) {
                Profile.frames.pop();
                this.end = getEllapsedTime();
                if (this.isLeaf) {
                    this.printShort();
                } else {
                    this.printEnd();
                }
            } else {
                throw new Error("Corrupted profile stack! Closing '" + this.domain + ": " + this.name + "' while edge of the stack is: '" + Profile.frames.peek().domain + ": " + Profile.frames.peek().name + "'");
            }
        }

        private static long getStartTime(final int pid) throws IOException {
            final String path = "/proc/" + pid + "/stat";
            final BufferedReader reader = new BufferedReader(new FileReader(path));
            final String stat;
            try {
                stat = reader.readLine();
            } finally {
                reader.close();
            }
            final String field2End = ") ";
            final String fieldSep = " ";
            final int fieldStartTime = 20;
            final int nanoSecInSec = 1000000000;
            try {
                final String[] fields = stat.substring(stat.lastIndexOf(field2End)).split(fieldSep);
                final long t = Long.parseLong(fields[fieldStartTime]);
                final int tckName = Class.forName("libcore.io.OsConstants").getField("_SC_CLK_TCK").getInt(null);
                final Object os = Class.forName("libcore.io.Libcore").getField("os").get(null);
                final long tck = (Long)os.getClass().getMethod("sysconf", Integer.TYPE).invoke(os, tckName);
                return t * nanoSecInSec / tck;
            } catch (final NumberFormatException e) {
                throw new IOException(e);
            } catch (final IndexOutOfBoundsException e) {
                throw new IOException(e);
            } catch (ReflectiveOperationException e) {
                throw new IOException(e);
            }
        }

        private static double getEllapsedTime() throws IOException {
            if (startTime == 0) {
                startTime = getStartTime(android.os.Process.myPid());
            }

            return (SystemClock.elapsedRealtimeNanos() - startTime) / 1000000.0;
        }
    }
}