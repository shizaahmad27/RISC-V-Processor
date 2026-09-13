package FiveStage
import chisel3._
import chisel3.experimental.MultiIOModule

class Execute extends MultiIOModule {
    val io = IO(
        new Bundle {
            val op1 = Input(UInt(32.W))
            val op2 = Input(UInt(32.W))
            val aluOp = Input(UInt(4.W))
            val aluResult = Output(UInt(32.W))
        }
    )

    val alu = Module(new ALU).io
    alu.op1 := io.op1
    alu.op2 := io.op2
    alu.aluOp := io.aluOp
    io.aluResult := alu.aluResult
  
}
