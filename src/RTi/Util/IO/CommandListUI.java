package RTi.Util.IO;

//import java.util.List;

/**
This interface provides methods to allow code to interact with a UI for a command list, beyond the
basic actions of the command list model (whatever is implemented).  For example, a method is included
to request the insert position for a command.  This may be indicated by selections in a UI, whereas the
basic list model does not know about UI state.  It is expected that this interface will remain fairly simple
but may grow over time.
*/
public interface CommandListUI {

/**
Insert a command in the command list, depending on the state of the UI (e.g., insert before highlighted
commands).
@param command single command to insert
*/
public void insertCommand ( Command command );

/**
Insert multiple commands in the command list, depending on the state of the UI (e.g., insert before highlighted
commands).
@param commands list of commands to insert
*/
// TODO SAM 2009-06-12 Enable when needed.
// public void insertCommands ( List<Command> commands );
	
}
