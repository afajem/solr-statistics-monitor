
import play.mvc.WebSocket;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Stubbed out <code>WebSocket.Out</code>.
 * 
 * Adapted from Reactive Stock Activator, by James Ward.
 * see code at: https://github.com/typesafehub/reactive-stocks/blob/master/test/actors/StubOut.scala
 * 
 */
public class StubWebSocketOut implements WebSocket.Out<JsonNode> {
	private JsonNode node;
	
	
	@Override
	public void write(JsonNode node) {
		this.node = node;
		
	}

	@Override
	public void close() {
	}

	
	public JsonNode getNode() {
		return node;
	}
}
