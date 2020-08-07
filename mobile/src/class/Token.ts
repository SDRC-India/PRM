import { TokenChar } from "./TokenChar";


export class Token {


    associativityMap : Map<String, String>  = new Map<String,String>()
	precedenceMap :Map<String, Number> = new Map<String,Number>()

	type  : TokenChar
	value : any;
	arguments:number=0;
	constructor(type : TokenChar, value) {
		this.type = type;
		this.value = value;

		this.associativityMap.set("^", "right");
		this.associativityMap.set("%", "left");
		this.associativityMap.set("*", "left");
		this.associativityMap.set("/", "left");
		this.associativityMap.set("+", "left");
		this.associativityMap.set("-", "left");
		this.associativityMap.set("<", "left");
		this.associativityMap.set("<=", "left");
		this.associativityMap.set(">", "left");
		this.associativityMap.set(">=", "left");
		this.associativityMap.set("=", "left");



		this.precedenceMap.set("^", 5);
		this.precedenceMap.set("%", 4);
		this.precedenceMap.set("*", 3);
		this.precedenceMap.set("/", 3);
		this.precedenceMap.set("+", 2);
		this.precedenceMap.set("-", 2);


	}

	precedence() : Number  {
		return this.precedenceMap.get(this.type.toString());
	}

	public associativity() : String {
		return this.associativityMap.get(this.type.toString());
	}

	
	print(){
		return "[Type= Token ="+ TokenChar[this.type] +",value="+this.value+",arguments:"+this.arguments+"]";
	}
	

}