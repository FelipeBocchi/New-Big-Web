export class Employee {

    id: number;
    name: String;
    cpf: String;
    phone: String;
    email: String;
    username: String;
    password: String;
    active: Boolean;
    admin: Boolean;
    createAt: Date;
    birthDate: Date;
    role: String;

    constructor( id: number, name: String, cpf: String, phone: String, email: String, username: String, password: String, active: Boolean, admin: Boolean, birthDate: Date, createAt: Date, role: String){
        this.id = id;
        this.name = name;
        this.cpf = cpf;
        this.phone = phone;
        this.email = email;
        this.username = username;
        this.password = password;
        this.active = active;
        this.admin = admin;
        this.birthDate = birthDate;
        this.createAt = createAt;
        this.role = role;
    }
    
}
