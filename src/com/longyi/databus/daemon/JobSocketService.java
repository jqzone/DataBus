package com.longyi.databus.daemon;

import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.zeromq.ZMQ;
import org.zeromq.ZMsg;
import org.zeromq.ZMQ.Context;

import com.longyi.databus.define.DATABUS;

public class JobSocketService {
	private static final String KeyServerEndpoint=DaemonMain.KeyServerEndpoint;
	private static final Context context=DATABUS.context;
	private static final Queue<ZMQ.Socket> freeSocList=new ConcurrentLinkedQueue<ZMQ.Socket>();
	private static final Queue<ZMQ.Socket> totalsync=new ConcurrentLinkedQueue<ZMQ.Socket>();
	private static final HashMap<String,Queue<ZMQ.Socket>> OuterServerMap=new HashMap<String,Queue<ZMQ.Socket>>();
	public JobSocketService()
	{
	};
	/*
	 * 通向keyserver的通道
	 */
	
	private ZMQ.Socket CreateNewDealerSocToKeyServer()
	{
		ZMQ.Socket tmpSoc=context.socket(ZMQ.DEALER);
		tmpSoc.connect(KeyServerEndpoint);
		return tmpSoc;
	}
	private synchronized ZMQ.Socket GetAFreeKeyServerDealerSoc()
	{
		if(!totalsync.isEmpty())
		{
			return totalsync.poll();
		}
		else
			return CreateNewDealerSocToKeyServer();
	};
	public void SendDealerRequestToKeyServer(ZMsg ReqeustMsg)
	{
		ZMQ.Socket SocketToKeyServer=GetAFreeKeyServerDealerSoc();
		ReqeustMsg.send(SocketToKeyServer);
		totalsync.offer(SocketToKeyServer);
	};
	private ZMQ.Socket CreateNewSocToKeyServer()
	{
		ZMQ.Socket tmpSoc=context.socket(ZMQ.REQ);
		tmpSoc.connect(KeyServerEndpoint);
		return tmpSoc;
	};
	private ZMQ.Socket GetAFreeKeyServerSoc()
	{
		if(!freeSocList.isEmpty())
		{
			return freeSocList.poll();
		}
		else
			return CreateNewSocToKeyServer();
	};
	public ZMsg SendRequestToKeyServer(ZMsg ReqeustMsg)
	{
		ZMQ.Socket SocketToKeyServer=GetAFreeKeyServerSoc();
		ReqeustMsg.send(SocketToKeyServer);
		ZMsg recvMsg=ZMsg.recvMsg(SocketToKeyServer);
		freeSocList.offer(SocketToKeyServer);
		return recvMsg;
	};
	
	/*
	 * 通向其他Daemon Server的通道
	 */
	private ZMQ.Socket CreateNewSocToOtherDaemon(String Location)
	{
		ZMQ.Socket tmpSoc=context.socket(ZMQ.REQ);
		System.out.println("============="+Location);
		tmpSoc.connect(Location);
		return tmpSoc;
	};
	private ZMQ.Socket GetAFreeOtherDaemonSoc(String Location)
	{
		Queue<ZMQ.Socket> _tmpSocList=OuterServerMap.get(Location);
		if(_tmpSocList!=null)
		{
			if(!_tmpSocList.isEmpty())
			{
				return _tmpSocList.poll();
			}
			else
				return CreateNewSocToOtherDaemon(Location);
		}
		else
			return CreateNewSocToOtherDaemon(Location);
	};
	public ZMsg SendRequestOtherDaemon(String Location,ZMsg ReqeustMsg)
	{
		ZMQ.Socket SocketToOtherDaemonServer=GetAFreeOtherDaemonSoc(Location);
		ReqeustMsg.send(SocketToOtherDaemonServer);
		//System.out.println("Send Request");
		ZMsg recvMsg=ZMsg.recvMsg(SocketToOtherDaemonServer);
		//System.out.println("alread recvmsg");
		Queue<ZMQ.Socket> _tmpSocList=OuterServerMap.get(Location);
		if(_tmpSocList!=null)
			_tmpSocList.offer(SocketToOtherDaemonServer);
		else
		{
			_tmpSocList=new ConcurrentLinkedQueue<ZMQ.Socket>();
			_tmpSocList.offer(SocketToOtherDaemonServer);
			OuterServerMap.put(Location, _tmpSocList);
		}
		//System.out.println("Send back");
		return recvMsg;
	};
}
