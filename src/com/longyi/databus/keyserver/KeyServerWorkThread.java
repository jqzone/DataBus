package com.longyi.databus.keyserver;


import org.zeromq.ZMQ;
import org.zeromq.ZMQException;
import org.zeromq.ZMsg;
import org.zeromq.ZMQ.Context;

import com.longyi.databus.define.DATABUS;

public class KeyServerWorkThread extends Thread{
	private Context context;
    //private final ZMQ.Poller poller;
	private final ZMQ.Socket worker;
	private final ZMQ.Socket pubsoc;
	KeyServerWorkThread(Context _context)
	{
		context=_context;
		//this.poller = context.poller(1);
        worker=context.socket(ZMQ.DEALER);
		worker.connect(DATABUS.KEYREQBACKEND);
		pubsoc=context.socket(ZMQ.PUSH);
		pubsoc.connect(DATABUS.KEYPUBBACKEND);
		//this.poller.register(worker, ZMQ.Poller.POLLIN);
	}
	@Override
	public void run()
	{
		while(!Thread.currentThread().isInterrupted())
		{
			try{
				//if (poller.poll(25000) < 1) {
                //    continue;
               // }

				// process a request
	            //if (poller.pollin(0))
	            //{
	            	
	            ZMsg UpdateMsg=ZMsg.recvMsg(worker);
	            if(UpdateMsg!=null)
	            {
	            	ZMsg BackMsg=new ZMsg();
	            	
	            	BackMsg.wrap(UpdateMsg.unwrap());
	            	ZMsg PubMsg=UpdateMsg.duplicate();
	            	int UpdateName=Integer.parseInt(UpdateMsg.pop().toString());
	            	//System.out.println("=======================================================================");
	            	//System.out.println("========================================"+UpdateName);
	            	switch(UpdateName)
	            	{
	            	case DATABUS.JOB_INSERT:
	            	{
	            		String JobId=UpdateMsg.pop().toString();
	            		String PartionId=UpdateMsg.pop().toString();
	            		String key=UpdateMsg.pop().toString();
	            		KeyServerDataCenter.JOB_INSERT(JobId,PartionId,key);
            			break;
	            	}
	            	case DATABUS.JOB_INSERT_PATION_LOCATION:
	            	{
	            		String JobId=UpdateMsg.pop().toString();
	            		String PartionId=UpdateMsg.pop().toString();
	            		String Location=UpdateMsg.pop().toString();
	            		KeyServerDataCenter.JOB_INSERT_PARTION_LOCATION(JobId,PartionId,Location);
            			break;
	            	}
	            	case DATABUS.JOB_GET_PATION_LIST:
	            	{
	            		String JobId=UpdateMsg.pop().toString();
	            		String rtv=KeyServerDataCenter.JOB_GET_PATION_LIST(JobId);
            			
	            		if(rtv!=null)
	            		{
	            			BackMsg.addLast(Integer.toString(DATABUS.SUCCESSFULLY));
	            			BackMsg.addLast(rtv);
	            		}
	            		else
	            		{
		            		BackMsg.addLast(Integer.toString(DATABUS.FAILED));
	            		}
            			BackMsg.send(worker);
            			break;
	            	}
	            	case DATABUS.JOB_GET_PATION_LOCATION_LIST:
	            	{
	            		String JobId=UpdateMsg.pop().toString();
	            		String PartionId=UpdateMsg.pop().toString();
	            		String rtv=KeyServerDataCenter.JOB_GET_PATION_LOCATION_LIST(JobId,PartionId);
	            		if(rtv!=null)
	            		{
	            			BackMsg.addLast(Integer.toString(DATABUS.SUCCESSFULLY));
	            			BackMsg.addLast(rtv);
	            		}
	            		else
	            		{
		            		BackMsg.addLast(Integer.toString(DATABUS.FAILED));
	            		}
	            		
            			BackMsg.send(worker);
            			break;
	            	}
	            	case DATABUS.JOB_GET_KEY_LIST:
	            	{
	            		String JobId=UpdateMsg.pop().toString();
	            		String PartionId=UpdateMsg.pop().toString();
	            		String rtv=KeyServerDataCenter.JOB_GET_KEY_LIST(JobId,PartionId);
	            		
	            		if(rtv!=null)
	            		{	
	            			BackMsg.addLast(Integer.toString(DATABUS.SUCCESSFULLY));
	            			BackMsg.addLast(rtv);
	            		}
	            		else
	            		{
	            			BackMsg.addLast(Integer.toString(DATABUS.FAILED));
	            		}
	            		
            			BackMsg.send(worker);
            			break;
	            	}
	            	case DATABUS.SEND_MESSAGE:
	            		{
	            			String key=UpdateMsg.pop().toString();
	            			String Location=UpdateMsg.pop().toString();
	            			KeyServerMain.MessageMap.put(key,Location);
	            			BackMsg.addLast(Integer.toString(DATABUS.SUCCESSFULLY));
	            			BackMsg.send(worker);
	            			PubMsg.send(pubsoc);
	            			break;
	            		}
	            	case DATABUS.FREE_MESSAGE:
	            		{
	            			String key=UpdateMsg.pop().toString();
	            			KeyServerMain.MessageMap.remove(key);
	            			BackMsg.addLast(Integer.toString(DATABUS.SUCCESSFULLY));
	            			BackMsg.send(worker);
	            			PubMsg.send(pubsoc);
	            			break;
	            		}
	            	case DATABUS.SEND_TO_CHANNEL:
		            	{
		            		String key=UpdateMsg.pop().toString();
	            			String Location=UpdateMsg.pop().toString();
	            			KeyServerMain.ChannelMap.put(key,Location);
	            			BackMsg.addLast(Integer.toString(DATABUS.SUCCESSFULLY));
	            			BackMsg.send(worker);
	            			PubMsg.send(pubsoc);
	            			break;
	            		}
	            	case DATABUS.FREE_CHANNEL:
		            	{
		            		String key=UpdateMsg.pop().toString();
		            		KeyServerMain.ChannelMap.remove(key);
	            			BackMsg.addLast(Integer.toString(DATABUS.SUCCESSFULLY));
	            			BackMsg.send(worker);
	            			PubMsg.send(pubsoc);
	            			break;
	            		}
	            	case DATABUS.INSERT_A_FILE:
		            	{
		            		String key=UpdateMsg.pop().toString();
		            		String Location=UpdateMsg.pop().toString();
	            			KeyServerMain.FileMap.put(key,Location);
	            			BackMsg.addLast(Integer.toString(DATABUS.SUCCESSFULLY));
	            			BackMsg.send(worker);
	            			PubMsg.send(pubsoc);
	            			break;
	            		}
	            	case DATABUS.FREE_A_FILE:
		            	{
		            		String key=UpdateMsg.pop().toString();
		            		KeyServerMain.FileMap.remove(key);
	            			BackMsg.addLast(Integer.toString(DATABUS.SUCCESSFULLY));
	            			BackMsg.send(worker);
	            			PubMsg.send(pubsoc);
	            			break;
	            		}
	            	case DATABUS.DELETE_A_FILE:
		            	{
		            		String key=UpdateMsg.pop().toString();
		            		KeyServerMain.FileMap.remove(key);
	            			BackMsg.addLast(Integer.toString(DATABUS.SUCCESSFULLY));
	            			BackMsg.send(worker);
	            			PubMsg.send(pubsoc);
	            			break;
	            		}
	            	case DATABUS.PUBSYS:
		            	{
		            		BackMsg.addLast(Integer.toString(DATABUS.SUCCESSFULLY));
	            			BackMsg.send(worker);
	            			PubMsg.send(pubsoc);
	            			break;
		            	}
	            	case DATABUS.GET_ALL_KEY_INFO:
		            	{
		            		BackMsg.addLast(KeyServerMain.MessageMap.toString());
		            		BackMsg.addLast(KeyServerMain.ChannelMap.toString());
		            		BackMsg.addLast(KeyServerMain.FileMap.toString());
		            		BackMsg.send(worker);
		            		break;
		            	}
	            	default:
            			break;
	            	}
	            }
	        } catch (ZMQException e) {
	            // context destroyed, exit
	            if (ZMQ.Error.ETERM.getCode() == e.getErrorCode()) {
	                break;
	            }
	            throw e;
	        }
		}
	}
}
